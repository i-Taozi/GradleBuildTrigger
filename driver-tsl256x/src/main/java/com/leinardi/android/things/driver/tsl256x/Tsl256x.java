/*
 * Copyright 2018 Roberto Leinardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leinardi.android.things.driver.tsl256x;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.IntDef;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Gain.GAIN_16X;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Gain.GAIN_1X;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATION_TIME_101MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATION_TIME_13_7MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATION_TIME_402MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.InterruptControl.INTERRUPT_CTRL_DISABLE;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.InterruptControl.INTERRUPT_CTRL_LEVEL;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.InterruptControl.INTERRUPT_CTRL_SMBALERT;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.InterruptControl.INTERRUPT_CTRL_TEST_MODE;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Mode.MODE_ACTIVE;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Mode.MODE_STANDBY;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.PackageType.CS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.PackageType.T_FN_CL;
import static java.lang.Math.pow;

/**
 * Driver for the TSL256x light-to-digital converter.
 */
@SuppressWarnings("WeakerAccess")
public class Tsl256x implements Closeable {
    public static final int I2C_ADDRESS = 0x39;
    public static final float MAX_RANGE_LUX = 40_000;
    public static final float MAX_POWER_CONSUMPTION_UA = 600;
    private static final String TAG = Tsl256x.class.getSimpleName();

    /**
     * Select Command Register.
     */
    private static final int COMMAND_BIT = 0b1000_0000;
    /**
     * Interrupt clear. Clears any pending interrupt. This bit is a write-one-to-clear bit. It is
     * self clearing.
     */
    private static final int CLEAR_BIT = 0b0100_0000;
    /**
     * SMB Write/Read Word Protocol. 1 indicates that this SMB transaction is using either
     * the SMB Write Word or Read Word protocol.
     */
    private static final int WORD_BIT = 0b0010_0000;
    /**
     * Block Write/Read Protocol. 1 indicates that this transaction is using either the Block
     * Write or the Block Read protocol.
     */
    private static final int BLOCK_BIT = 0b0001_0000;

    /**
     * The Control Register contains two bits and is primarily used to
     * power the TSL256x device up and down.
     */
    private static final int REGISTER_CONTROL = 0x00;
    /**
     * The Timing Register controls both the integration time and the
     * gain of the ADC channels. A common set of control bits is
     * provided that controls both ADC channels. The Timing Register
     * defaults to 02h at power on.
     */
    private static final int REGISTER_TIMING = 0x01;
    /**
     * The Interrupt Threshold registers store the values to be used as
     * the high and low trigger points for the comparison function for
     * interrupt generation. If the value generated by channel 0
     * crosses below or is equal to the low threshold specified, an
     * interrupt is asserted on the interrupt pin. If the value generated
     * by channel 0 crosses above the high threshold specified, an
     * interrupt is asserted on the interrupt pin. Registers
     * THRESHLOWLOW and THRESHLOWHIGH provide the low byte
     * and high byte, respectively, of the lower interrupt threshold.
     * Registers THRESHHIGHLOW and THRESHHIGHHIGH provide the
     * low and high bytes, respectively, of the upper interrupt
     * threshold. The high and low bytes from each set of registers are
     * combined to form a 16-bit threshold value. The interrupt
     * threshold registers default to 00h on power up.
     */
    private static final int REGISTER_THRESHHOLDL_LOW = 0x02;  // Interrupt low threshold low-byte
    private static final int REGISTER_THRESHHOLDL_HIGH = 0x03; // Interrupt low threshold high-byte
    private static final int REGISTER_THRESHHOLDH_LOW = 0x04;  // Interrupt high threshold low-byte
    private static final int REGISTER_THRESHHOLDH_HIGH = 0x05; // Interrupt high threshold high-byte
    /**
     * The Interrupt Register controls the extensive interrupt
     * capabilities of the TSL256x. The TSL256x permits both
     * SMB-Alert style interrupts as well as traditional level-style
     * interrupts. The interrupt persist bit field (PERSIST) provides
     * control over when interrupts occur. A value of 0 causes an
     * interrupt to occur after every integration cycle regardless of the
     * threshold settings. A value of 1 results in an interrupt after one
     * integration time period outside the threshold window. A value
     * of N (where N is 2 through15) results in an interrupt only if the
     * value remains outside the threshold window for N consecutive
     * integration cycles. For example, if N is equal to 10 and the
     * integration time is 402ms, then the total time is approximately
     * 4 seconds.
     */
    private static final int REGISTER_INTERRUPT = 0x06;
    private static final int REGISTER_CRC = 0x08;              // Factory use only
    /**
     * The ID Register provides the value for both the part number and
     * silicon revision number for that part number. It is a read-only
     * register, whose value never changes.
     */
    private static final int REGISTER_ID = 0x0A;
    /**
     * The ADC channel data are expressed as 16-bit values spread
     * across two registers. The ADC channel 0 data registers,
     * DATA0LOW and DATA0HIGH provide the lower and upper bytes,
     * respectively, of the ADC value of channel 0. Registers
     * DATA1LOW and DATA1HIGH provide the lower and upper bytes,
     * respectively, of the ADC value of channel 1. All channel data
     * registers are read-only and default to 00h on power up.
     */
    private static final int REGISTER_DATA0_LOW = 0x0C;        // Light data channel 0, low byte
    private static final int REGISTER_DATA0_HIGH = 0x0D;       // Light data channel 0, high byte
    private static final int REGISTER_DATA1_LOW = 0x0E;        // Light data channel 1, low byte
    private static final int REGISTER_DATA1_HIGH = 0x0F;       // Light data channel 1, high byte

    private static final int ID_PART_NUMBER = 0b1111_0000;
    private static final int ID_REVISION_NUMBER = 0b0000_1111;
    private static final int TSL2560_ID = 0b0000_0000;
    private static final int TSL2561_ID = 0b0001_0000;
    private static final int TSL2562_ID = 0b0010_0000;
    private static final int TSL2563_ID = 0b0011_0000;
    private static final int TSL2560T_FN_CL_ID = 0b0100_0000;
    private static final int TSL2561T_FN_CL_ID = 0b0101_0000;

    // Auto-gain thresholds
    private static final int AGC_THI_13MS = 4850;    // Max value at Ti 13ms = 5047
    private static final int AGC_TLO_13MS = 100;     // Min value at Ti 13ms = 100
    private static final int AGC_THI_101MS = 36000;  // Max value at Ti 101ms = 37177
    private static final int AGC_TLO_101MS = 200;    // Min value at Ti 101ms = 200
    private static final int AGC_THI_402MS = 63000;  // Max value at Ti 402ms = 65535
    private static final int AGC_TLO_402MS = 500;    // Min value at Ti 402ms = 500

    private byte mChipId;
    private boolean mAutoGain;
    @IntegrationTime
    private int mIntegrationTime;
    @Gain
    private int mGain;
    @PackageType
    private int mPackageType;
    private I2cDevice mDevice;
    private boolean mAllowSleep;

    /**
     * Create a new TSL2561 driver connected to the given I2C bus.
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @throws IOException
     */
    public Tsl256x(String i2cName, int i2cAddress) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        I2cDevice device = pioService.openI2cDevice(i2cName, i2cAddress);
        try {
            connect(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;

        @PackageType int packageType = CS;

        mChipId = readRegByte(REGISTER_ID);
        int partNumber = (mChipId & ID_PART_NUMBER) & 0xFF;
        switch (partNumber) {
            case TSL2560_ID:
                Log.d(TAG, "Found TSL2560");
                break;
            case TSL2561_ID:
                Log.d(TAG, "Found TSL2561");
                break;
            case TSL2562_ID:
                Log.d(TAG, "Found TSL2562");
                break;
            case TSL2563_ID:
                Log.d(TAG, "Found TSL2563");
                break;
            case TSL2560T_FN_CL_ID:
                Log.d(TAG, "Found TSL2560T/FN/CL");
                packageType = T_FN_CL;
                break;
            case TSL2561T_FN_CL_ID:
                Log.d(TAG, "Found TSL2561T/FN/CL");
                packageType = T_FN_CL;
                break;
            default:
                throw new IllegalStateException("Could not find a TSL256x, check wiring!");
        }

        setPackageType(packageType);
        setIntegrationTime(INTEGRATION_TIME_402MS);
        setGain(GAIN_16X);
        setAllowSleep(true);

        /* Note: by default, the device is in power down mode on bootup */
        setMode(MODE_STANDBY);
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Gets the value for both the part number and silicon revision number for that part number.
     */
    public byte getChipId() {
        return mChipId;
    }

    @PackageType
    public int getPackageType() {
        return mPackageType;
    }

    public void setPackageType(@PackageType int packageType) {
        mPackageType = packageType;
    }

    /**
     * Returns true if the auto-gain settings is enable, false if is disable.
     */
    public boolean isAutoGainEnabled() {
        return mAutoGain;
    }

    /**
     * Enables or disables the auto-gain settings when reading data from the sensor.
     *
     * @param autoGain Set to true to enable, false to disable
     */
    public void setAutoGain(boolean autoGain) {
        mAutoGain = autoGain;
    }

    /**
     * Sets the integration time. Higher time means more light captured (better for low light conditions) but will
     * take longer to run readings.
     *
     * @param integrationTime See {@link IntegrationTime}.
     * @throws IOException
     */
    public void setIntegrationTime(@IntegrationTime int integrationTime) throws IOException {
        setMode(MODE_ACTIVE);
        writeRegByte(COMMAND_BIT | REGISTER_TIMING, (byte) ((integrationTime | mGain) & 0xFF));
        setMode(MODE_STANDBY);
        mIntegrationTime = integrationTime;
    }

    /**
     * Adjusts the gain (adjusts the sensitivity to light).
     *
     * @param gain See {@link Gain}
     * @throws IOException
     */
    public void setGain(@Gain int gain) throws IOException {
        setMode(MODE_ACTIVE);
        writeRegByte(COMMAND_BIT | REGISTER_TIMING, (byte) ((mIntegrationTime | gain) & 0xFF));
        setMode(MODE_STANDBY);
        mGain = gain;
    }

    /**
     * Gets the broadband (mixed lighting) and IR only values, adjusting gain if auto-gain is enabled
     *
     * @return an array containing the broadband (channel 0) on index 0 and IR (channel 1) on index 1.
     * @throws IOException
     */
    public int[] readLuminosity() throws IOException {
        setMode(MODE_ACTIVE);
        // Wait x ms for ADC to complete
        switch (mIntegrationTime) {
            case INTEGRATION_TIME_13_7MS:
                SystemClock.sleep(50);
                break;
            case INTEGRATION_TIME_101MS:
                SystemClock.sleep(150);
                break;
            case INTEGRATION_TIME_402MS:
                SystemClock.sleep(450);
                break;
        }
        int[] luminosities;
        if (isAutoGainEnabled()) {
            boolean check = false;
            boolean validRangeFound = false;

            do {
                int ch0;
                int tresholdHigh = 0;
                int tresholdLow = 0;
                @IntegrationTime
                int integrationTime = mIntegrationTime;

                // Get the hi/low threshold for the current integration time
                switch (integrationTime) {
                    case INTEGRATION_TIME_13_7MS:
                        tresholdHigh = AGC_THI_13MS;
                        tresholdLow = AGC_TLO_13MS;
                        break;
                    case INTEGRATION_TIME_101MS:
                        tresholdHigh = AGC_THI_101MS;
                        tresholdLow = AGC_TLO_101MS;
                        break;
                    case INTEGRATION_TIME_402MS:
                        tresholdHigh = AGC_THI_402MS;
                        tresholdLow = AGC_TLO_402MS;
                        break;
                }
                luminosities = readLuminosityData();
                ch0 = luminosities[0];

                // Run an auto-gain check if we haven't already done so...
                if (!check) {
                    if ((ch0 < tresholdLow) && (mGain == GAIN_1X)) {
                        // Increase the gain and try again
                        setGain(GAIN_16X);
                        // Drop the previous conversion results
                        luminosities = readLuminosityData();
                        // Set a flag to indicate we've adjusted the gain
                        check = true;
                    } else if ((ch0 > tresholdHigh) && (mGain == GAIN_16X)) {
                        // Drop gain to 1x and try again
                        setGain(GAIN_1X);
                        // Drop the previous conversion results
                        luminosities = readLuminosityData();
                        // Set a flag to indicate we've adjusted the gain
                        check = true;
                    } else {
                        // Nothing to look at here, keep moving ....
                        // Reading is either valid, or we're already at the chips limits
                        validRangeFound = true;
                    }
                } else {
                    // If we've already adjusted the gain once, just return the new results.
                    // This avoids endless loops where a value is at one extreme pre-gain,
                    // and the the other extreme post-gain
                    validRangeFound = true;
                }
            } while (!validRangeFound);
        } else {
            luminosities = readLuminosityData();
        }
        setMode(MODE_STANDBY);
        return luminosities;
    }

    private int[] readLuminosityData() throws IOException {
        int[] luminosities = new int[3];
        luminosities[0] = readRegWord(COMMAND_BIT | WORD_BIT | REGISTER_DATA0_LOW) & 0xFFFF;
        luminosities[1] = readRegWord(COMMAND_BIT | WORD_BIT | REGISTER_DATA1_LOW) & 0xFFFF;
        luminosities[2] = luminosities[0] - luminosities[1];
        return luminosities;
    }

    public float readLux() throws IOException {
        int[] luminosities = readLuminosity();
        // Convert from unsigned integer to floating point
        float ch0 = luminosities[0];
        float ch1 = luminosities[1];

        // We will need the ratio for subsequent calculations
        float ratio = ch1 / ch0;

        float time = 0;
        switch (mIntegrationTime) {
            case INTEGRATION_TIME_13_7MS:
                time = 13.7f;
                break;
            case INTEGRATION_TIME_101MS:
                time = 101f;
                break;
            case INTEGRATION_TIME_402MS:
                time = 402f;
                break;
        }

        // Normalize for integration time
        ch0 *= (402.0 / time);
        ch1 *= (402.0 / time);

        // Normalize for gain
        if (mGain == GAIN_1X) {
            ch0 *= 16;
            ch1 *= 16;
        }

        // Determine lux per datasheet equations
        float lux = 0;
        if (mPackageType == CS) {
            if (ratio < 0.52) {
                lux = 0.0315f * ch0 - 0.0593f * ch0 * (float) pow(ratio, 1.4);
            } else if (ratio < 0.65) {
                lux = 0.0229f * ch0 - 0.0291f * ch1;
            } else if (ratio < 0.80) {
                lux = 0.0157f * ch0 - 0.0180f * ch1;
            } else if (ratio < 1.30) {
                lux = 0.00338f * ch0 - 0.00260f * ch1;
            }
        } else {
            if (ratio < 0.5) {
                lux = 0.0304f * ch0 - 0.062f * ch0 * (float) pow(ratio, 1.4);
            } else if (ratio < 0.61) {
                lux = 0.0224f * ch0 - 0.031f * ch1;
            } else if (ratio < 0.80) {
                lux = 0.0128f * ch0 - 0.0153f * ch1;
            } else if (ratio < 1.30) {
                lux = 0.00146f * ch0 - 0.00112f * ch1;
            }
        }
        return lux;
    }

    /**
     * Set current power mode.
     *
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setMode(@Mode int mode) throws IOException, IllegalStateException {
        if (mAllowSleep || mode == MODE_ACTIVE) {
            writeRegByte(COMMAND_BIT | REGISTER_CONTROL, (byte) mode);
        }
    }

    public boolean isAllowSleep() {
        return mAllowSleep;
    }

    public void setAllowSleep(boolean allowSleep) throws IOException {
        if (!allowSleep) {
            setMode(MODE_ACTIVE);
        }
        mAllowSleep = allowSleep;
    }

    /**
     * Sets up interrupt control.
     * <p>
     * Important: sleep mode must be disable. See {@link #setAllowSleep(boolean)}.
     * <p>
     * A persist value of 0 causes an interrupt to occur after every integration cycle regardless
     * of the threshold settings. A value of 1 results in an interrupt after one integration
     * time period outside the threshold window. A value of N (where N is 2 through 15) results
     * in an interrupt only if the value remains outside the threshold window for N consecutive
     * integration cycles. For example, if N is equal to 10 and the integration time is 402 ms,
     * then the total time is approximately 4 seconds.
     *
     * @param interruptControl See {@link InterruptControl}
     * @param persist          0, every integration cycle generates an interrupt; 1, any value outside of threshold
     *                         generates an interrupt; 2 to 15, value must be outside of threshold for 2 to 15
     *                         integration cycles
     * @throws IOException
     */
    public void setInterruptControl(@InterruptControl int interruptControl, int persist) throws IOException {
        if (persist < 0 || persist > 0b1111) {
            throw new IllegalArgumentException("persist must be between 0 and 15 inclusive. persist: " + persist);
        }
        setMode(MODE_ACTIVE);
        writeRegByte(COMMAND_BIT | REGISTER_INTERRUPT, (byte) ((interruptControl) | (persist)));
        // NOTE: This disables interrupts if mAllowSleep is true
        setMode(MODE_STANDBY);
    }

    /**
     * Sets interrupt thresholds (TSL2561 supports only interrupts generated by thresholds on channel 0).
     * <p>
     * Important: values supplied as thresholds are raw sensor values (see {@link #readLuminosity()}, and NOT values
     * in the SI lux unit.
     *
     * @param lowThreshold
     * @param highThreshold
     * @throws IOException
     */
    public void setInterruptThreshold(short lowThreshold, short highThreshold) throws IOException {
        setMode(MODE_ACTIVE);
        writeRegWord(COMMAND_BIT | REGISTER_THRESHHOLDL_LOW, lowThreshold);
        writeRegWord(COMMAND_BIT | REGISTER_THRESHHOLDH_LOW, highThreshold);
        setMode(MODE_STANDBY);
    }

    /**
     * Clears an active interrupt.
     */
    public void clearLevelInterrupt() throws IOException {
        // Send command byte for interrupt clear
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        mDevice.write(new byte[]{(byte) (COMMAND_BIT | CLEAR_BIT)}, 1);
    }

    private byte readRegByte(int reg) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        return mDevice.readRegByte(reg);
    }

    private void writeRegByte(int reg, byte data) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        mDevice.writeRegByte(reg, data);
    }

    private short readRegWord(int reg) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        return mDevice.readRegWord(reg);
    }

    private void writeRegWord(int reg, short data) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        mDevice.writeRegWord(reg, data);
    }

    /**
     * The amount of time we'd like to add up values.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INTEGRATION_TIME_13_7MS,
            INTEGRATION_TIME_101MS,
            INTEGRATION_TIME_402MS
    })
    public @interface IntegrationTime {
        int INTEGRATION_TIME_13_7MS = 0b0000_0000;  // 13.7ms
        int INTEGRATION_TIME_101MS = 0b0000_0001; // 101ms
        int INTEGRATION_TIME_402MS = 0b0000_0010; // 402ms
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GAIN_1X, GAIN_16X
    })
    public @interface Gain {
        int GAIN_1X = 0b0000_0000;     // No gain
        int GAIN_16X = 0b0001_0000;    // 16x gain
    }

    /**
     * Power mode.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_STANDBY, MODE_ACTIVE})
    public @interface Mode {
        int MODE_STANDBY = 0b0000_0000; // i2c on, output off, low power
        int MODE_ACTIVE = 0b0000_0011;  // i2c on, output on
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CS, T_FN_CL})
    public @interface PackageType {
        int CS = 0;
        int T_FN_CL = 3;
    }

    /**
     * {@link #INTERRUPT_CTRL_DISABLE}: Interrupt output disabled
     * {@link #INTERRUPT_CTRL_LEVEL}: When a level Interrupt is selected, an interrupt is generated whenever the last
     * conversion results in a value outside of the programmed threshold window. The interrupt is active-low and
     * remains asserted until cleared by writing the Command Register with the CLEAR bit set.
     * {@link #INTERRUPT_CTRL_SMBALERT}: In SMBAlert mode, the interrupt is similar to the traditional level style
     * and the interrupt line is asserted low. To clear the interrupt, the host responds to the SMBAlert by
     * performing a modified Receive Byte operation, in which the Alert Response Address (ARA) is placed in the slave
     * address field, and the TSL256x that generated the interrupt responds by returning its own address in the seven
     * most significant bits of the receive data byte. If more than one device connected on the bus has pulled the
     * SMBAlert line low, the highest priority (lowest address) device will win communication rights via standard
     * arbitration during the slave address transfer. If the device loses this arbitration, the interrupt will not be
     * cleared. The Alert Response Address is 0Ch.
     * {@link #INTERRUPT_CTRL_TEST_MODE}: The interrupt is generated immediately following the SMBus write operation.
     * Operation then behaves in an SMBAlert mode, and the software set interrupt may be cleared by an SMBAlert cycle.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INTERRUPT_CTRL_DISABLE, INTERRUPT_CTRL_LEVEL, INTERRUPT_CTRL_SMBALERT, INTERRUPT_CTRL_TEST_MODE})
    public @interface InterruptControl {
        int INTERRUPT_CTRL_DISABLE = 0b0000_0000;
        int INTERRUPT_CTRL_LEVEL = 0b0000_0001;
        int INTERRUPT_CTRL_SMBALERT = 0b0000_0010;
        int INTERRUPT_CTRL_TEST_MODE = 0b0000_0011;
    }
}
