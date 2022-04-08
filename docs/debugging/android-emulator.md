# Setting up the Android Emulator

The Android Emulator doesn't support USB.  Instead, you'll need to:

- debug using a TCP connection to your Proxmark3 (this document), or,
- [use something like VirtualBox which supports USB](./virtualbox.md).

This example is written with `socat` in mind, but should work with anything which can run a TCP
server which redirects all communications to the PM3.

TCP mode has _no transport security or authentication_, and will allow anything that can make an IP
connection to control your PM3.  The timeouts are also extremely short -- and are probably
unsuitable for anything except local-loopback or LAN communication.

## Exposing the PM3 over TCP

These examples open a TCP server on `localhost` port `1234`.

### Using `socat`

> **Note:** On non-Linux platforms, replace `/dev/ttyACM0` with the path to your PM3 device
> (eg: for macOS, it is something like `/dev/tty.usbmodem1234`)

```sh
socat -dd tcp-listen:1234,bind=127.0.0.1,reuseaddr \
    open:/dev/ttyACM0,raw,echo=0,append=0,nonblock=1,noctty=1,clocal=1,cs8,ixoff=0,ixon=0
```

## Connecting using AndProx

AndProx attempts to detect the Android Emulator, and fills in the IP automatically [which
corresponds to `127.0.0.1` on the host][emu-netaddr] (`10.0.2.2`).

If this has failed for some reason, you can fill this in manually.


[emu-netaddr]: https://developer.android.com/studio/run/emulator-networking#networkaddresses