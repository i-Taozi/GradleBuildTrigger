/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.http.protocol;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.health.meter.MeterActiveTime;
import com.caucho.v5.health.meter.MeterAverage;
import com.caucho.v5.health.meter.MeterService;
import com.caucho.v5.http.container.HttpContainer;
import com.caucho.v5.http.dispatch.Invocation;
import com.caucho.v5.http.protocol2.ConnectionHttp2;
import com.caucho.v5.http.protocol2.Http2Constants;
import com.caucho.v5.io.ClientDisconnectException;
import com.caucho.v5.io.ReadStream;
import com.caucho.v5.io.TempBuffer;
import com.caucho.v5.io.WriteStream;
import com.caucho.v5.network.port.ConnectionProtocol;
import com.caucho.v5.util.CharBuffer;
import com.caucho.v5.util.CharSegment;
import com.caucho.v5.util.CurrentTime;
import com.caucho.v5.util.L10N;
import com.caucho.v5.web.CookieWeb;
import com.caucho.v5.web.webapp.RequestBaratine;

import io.baratine.io.Buffer;

/**
 * Parses and holds request information for an HTTP request.
 */
public class RequestHttp1 extends RequestHttpBase
{
  private static final L10N L = new L10N(RequestHttp1.class);
  
  private static final Logger log
    = Logger.getLogger(RequestHttp1.class.getName());
  
  public static final int HTTP_0_9 = 0x0009;
  public static final int HTTP_1_0 = 0x0100;
  public static final int HTTP_1_1 = 0x0101;

  private static final CharBuffer _getCb = new CharBuffer("GET");
  private static final CharBuffer _headCb = new CharBuffer("HEAD");
  private static final CharBuffer _postCb = new CharBuffer("POST");
  
  private static final byte []_http10ok = "HTTP/1.0 200 OK".getBytes();
  private static final byte []_http11ok = "HTTP/1.1 200 OK".getBytes();
  private static final byte []_contentLengthBytes = "\r\ncontent-length: ".getBytes();
  private static final byte []_contentTypeBytes = "\r\ncontent-type: ".getBytes();
  private static final byte []_textHtmlBytes = "\r\ncontent-type: text/html".getBytes();
  private static final byte []_charsetBytes = "; charset=".getBytes();
  private static final byte []_transferChunkedBytes = "\r\ntransfer-encoding: chunked".getBytes();

  private static final byte []_connectionCloseBytes = "\r\nconnection: close".getBytes();

  //private static final char []_connectionCb = "connection".toCharArray();
  //private static final CharBuffer _closeCb = new CharBuffer("close");

  private byte []_serverHeaderBytes;
  
  private static final char []_toLowerAscii;
  private static final char []_toUpperAscii;
  private static final boolean []_isHttpWhitespace;

  private static final String METER_REQUEST_TIME
    = "Caucho|Http|Request";
  private static final String METER_REQUEST_READ_BYTES
    = "Caucho|Http|Request Read Bytes";
  private static final String METER_REQUEST_WRITE_BYTES
    = "Caucho|Http|Request Write Bytes";

  // private RequestProtocol _subrequest;
  
  private final CharBuffer _method     // "GET"
    = new CharBuffer();
  private String _methodString;

  private final CharBuffer _uriHost    // www.caucho.com:8080
    = new CharBuffer();
  private CharSequence _host;
  
  private byte []_uri;                 // "/path/test.jsp/Junk?query=7"
  private int _uriLength;

  private final CharBuffer _protocol   // "HTTP/1.0"
    = new CharBuffer();
  private int _version;
  
  private char []_headerBuffer;
  private int _headerLength;

  private CharSegment []_headerKeys;
  private CharSegment []_headerValues;
  private int _headerSize;

  private InChunked _chunkedInputStream = new InChunked();
  private InContentLength _contentLengthStream = new InContentLength();
  // private InRaw _rawInputStream = new InRaw();
  
  private boolean _isChunkedIn; 
  private long _inOffset;
  
  private KeepaliveState _keepalive = KeepaliveState.INIT;

  private MeterActiveTime _meterRequestTime;
  private MeterAverage _meterRequestReadBytes;
  private MeterAverage _meterRequestWriteBytes;

  /*
  private final CharBuffer _cb = new CharBuffer();
  private final StringBuilder _sb = new StringBuilder();
  */
  
  /*
  private final byte []_dateBuffer = new byte[256];
  private final CharBuffer _dateCharBuffer = new CharBuffer();

  private int _dateBufferLength;
  private long _lastDate;
  */
  
  //private WriteStream _rawWrite;

  //private OutHttpProxy _outProxy;

  private boolean _isUpgrade;

  private PendingFirst _pending;

  // private final RequestHttpState _state;

  private boolean _isFirst;

  private long _sequence;
  
  private StateAlloc _state = StateAlloc.NEW;
  
  // private HmuxRequest _hmuxRequest;

  /**
   * Creates a new HttpRequest.  New connections reuse the request.
   *
   * @param server the owning server.
   */
  public RequestHttp1(ProtocolHttp protocolHttp)
  {
    super(protocolHttp);
    
    //Objects.requireNonNull(state);
    
    //_state = state;

    _meterRequestTime
      = MeterService.createActiveTimeMeter(METER_REQUEST_TIME);

    _meterRequestReadBytes
      = MeterService.createAverageMeter(METER_REQUEST_READ_BYTES, "");

    _meterRequestWriteBytes
      = MeterService.createAverageMeter(METER_REQUEST_WRITE_BYTES, "");
    
    String serverHeader = protocolHttp().serverHeader();
    
    _serverHeaderBytes = ("\r\nserver: " + serverHeader).getBytes();
  }
  
  @Override
  public void init(RequestBaratine request)
  {
    super.init(request);
    
    initRequest();
  }

  /**
   * Returns true for the top-level request, but false for any include()
   * or forward()
   */
  /*
  public boolean isTop()
  {
    return true;
  }
  */

  /*
  protected boolean checkLogin()
  {
    return true;
  }
  */

  //
  // HTTP request properties
  //

  /**
   * Returns a buffer containing the request method.
   */
  public CharSegment getMethodBuffer()
  {
    return _method;
  }

  /**
   * Returns the HTTP method (GET, POST, HEAD, etc.)
   */
  @Override
  public String method()
  {
    if (_methodString == null) {
      CharSegment cb = getMethodBuffer();
      if (cb.length() == 0) {
        _methodString = "GET";
        return _methodString;
      }

      switch (cb.charAt(0)) {
      case 'G':
        _methodString = cb.equals(_getCb) ? "GET" : cb.toString();
        break;

      case 'H':
        _methodString = cb.equals(_headCb) ? "HEAD" : cb.toString();
        break;

      case 'P':
        _methodString = cb.equals(_postCb) ? "POST" : cb.toString();
        break;

      default:
        _methodString = cb.toString();
      }
    }

    return _methodString;
  }

  /**
   * Returns the virtual host of the request
   */
  @Override
  protected CharSequence getHost()
  {
    if (_host != null)
      return _host;

    if (_uriHost.length() > 0) {
      _host = _uriHost;
    }
    else if ((_host = getForwardedHostHeader()) != null) {
    }
    else {
      _host = getHostHeader();
    }

    return _host;
  }

  /**
   * Returns the virtual host from the invocation
   */
  private CharSequence hostInvocation()
    throws IOException
  {
    if (_host != null) {
    }
    else if (_uriHost.length() > 0) {
      _host = _uriHost;
    }
    else if ((_host = getForwardedHostHeader()) != null) {
    }
    else if ((_host = getHostHeader()) != null) {
    }
    else if (HTTP_1_1 <= version()) {
      throw new BadRequestException(L.l("HTTP/1.1 requires a Host header (Remote IP={0})", 
                                        connTcp().ipRemote()));
    }

    return _host;
  }

  /**
   * Returns the byte buffer containing the request URI
   */
  @Override
  public byte []uriBuffer()
  {
    return _uri;
  }

  /**
   * Returns the length of the request URI
   */
  @Override
  public int uriLength()
  {
    return _uriLength;
  }

  /**
   * Returns the protocol.
   */
  @Override
  public String getProtocol()
  {
    switch (_version) {
    case HTTP_1_1:
      return "HTTP/1.1";
    case HTTP_1_0:
      return "HTTP/1.0";
    case HTTP_0_9:
    default:
      return "HTTP/0.9";
    }
  }

  /**
   * Returns a char segment containing the protocol.
   */
  public CharSegment getProtocolBuffer()
  {
    return _protocol;
  }

  /**
   * Returns the HTTP version of the request based on getProtocol().
   */
  int version()
  {
    if (_version > 0) {
      return _version;
    }

    CharSegment protocol = getProtocolBuffer();
    if (protocol.equals("HTTP/1.1")) {
      _version = HTTP_1_1;
      return HTTP_1_1;
    }
    else if (protocol.equals("HTTP/1.0")) {
      _version = HTTP_1_0;
      return _version;
    }
    else if (protocol.equals("HTTP/0.9")) {
      _version = HTTP_0_9;
      return HTTP_0_9;
    }
    else if (protocol.length() < 8) {
      _version = HTTP_0_9;
      return _version;
    }

    int i = protocol.indexOf('/');
    int len = protocol.length();
    int major = 0;
    for (i++; i < len; i++) {
      char ch = protocol.charAt(i);

      if ('0' <= ch && ch <= '9')
        major = 10 * major + ch - '0';
      else if (ch == '.')
        break;
      else {
        _version = HTTP_1_0;
        return _version;
      }
    }

    int minor = 0;
    for (i++; i < len; i++) {
      char ch = protocol.charAt(i);

      if ('0' <= ch && ch <= '9')
        minor = 10 * minor + ch - '0';
      else
        break;
    }

    _version = 256 * major + minor;

    return _version;
  }

  //
  // HTTP request headers
  //

  /**
   * Returns the header.
   */
  @Override
  public String header(String key)
  {
    CharSegment buf = getHeaderBuffer(key);
    
    if (buf != null)
      return buf.toString();
    else
      return null;
  }

  /**
   * Returns the number of headers.
   */
  @Override
  public int getHeaderSize()
  {
    return _headerSize;
  }

  /**
   * Returns the header key
   */
  @Override
  public CharSegment getHeaderKey(int index)
  {
    return _headerKeys[index];
  }

  /**
   * Returns the header value
   */
  @Override
  public CharSegment getHeaderValue(int index)
  {
    return _headerValues[index];
  }

  /**
   * Returns the matching header.
   *
   * @param testBuf header key
   * @param length length of the key.
   */
  public CharSegment getHeaderBuffer(char []testBuf, int length)
  {
    char []keyBuf = _headerBuffer;
    CharSegment []headerKeys = _headerKeys;
    
    char []toLowerAscii = _toLowerAscii;

    for (int i = _headerSize - 1; i >= 0; i--) {
      CharSegment key = headerKeys[i];

      if (key.length() != length)
        continue;

      int offset = key.offset();
      int j;
      
      for (j = length - 1; j >= 0; j--) {
        char a = testBuf[j];
        char b = keyBuf[offset + j];
        
        if (a == b) {
          continue;
        }
        else if (toLowerAscii[a] != toLowerAscii[b]) {
          break;
        }
      }

      if (j < 0) {
        return _headerValues[i];
      }
    }

    return null;
  }

  /**
   * Returns the header value for the key, returned as a CharSegment.
   */
  @Override
  public CharSegment getHeaderBuffer(String key)
  {
    int i = matchNextHeader(0, key);

    if (i >= 0) {
      return _headerValues[i];
    }
    else {
      return null;
    }
  }

  /**
   * Fills an ArrayList with the header values matching the key.
   *
   * @param values ArrayList which will contain the maching values.
   * @param key the header key to select.
   */
  @Override
  public void getHeaderBuffers(String key, ArrayList<CharSegment> values)
  {
    int i = -1;
    
    while ((i = matchNextHeader(i + 1, key)) >= 0) {
      values.add(_headerValues[i]);
    }
  }

  /**
   * Return an enumeration of headers matching a key.
   *
   * @param key the header key to match.
   * @return the enumeration of the headers.
   */
  @Override
  public Enumeration<String> getHeaders(String key)
  {
    ArrayList<String> values = new ArrayList<String>();
    
    int i = -1;
    while ((i = matchNextHeader(i + 1, key)) >= 0) {
      values.add(_headerValues[i].toString());
    }

    return Collections.enumeration(values);
  }

  /**
   * Returns the index of the next header matching the key.
   *
   * @param i header index to start search
   * @param key header key to match
   *
   * @return the index of the next header matching, or -1.
   */
  private int matchNextHeader(int i, String key)
  {
    int size = _headerSize;
    int length = key.length();

    char []keyBuf = _headerBuffer;
    char []toLowerAscii = _toLowerAscii;

    for (; i < size; i++) {
      CharSegment header = _headerKeys[i];

      if (header.length() != length)
        continue;

      int offset = header.offset();

      int j;
      for (j = 0; j < length; j++) {
        char a = key.charAt(j);
        char b = keyBuf[offset + j];
        
        if (a == b) {
          continue;
        }
        else if (toLowerAscii[a] != toLowerAscii[b]) {
          break;
        }
      }

      if (j == length) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Adds a new header.  Used only by the caching to simulate
   * If-Modified-Since.
   *
   * @param key the key of the new header
   * @param value the value for the new header
   */
  @Override
  public void setHeader(String key, String value)
  {
    int tail;

    if (_headerSize > 0) {
      tail = (_headerValues[_headerSize - 1].offset()
              + _headerValues[_headerSize - 1].length());
    }
    else {
      tail = 0;
    }

    int keyLength = key.length();
    int valueLength = value.length();
    char []headerBuffer = _headerBuffer;
    
    for (int i = keyLength - 1; i >= 0; i--) {
      headerBuffer[tail + i] = key.charAt(i);
    }

    _headerKeys[_headerSize].init(headerBuffer, tail, keyLength);

    tail += keyLength;

    for (int i = valueLength - 1; i >= 0; i--) {
      headerBuffer[tail + i] = value.charAt(i);
    }

    _headerValues[_headerSize].init(headerBuffer, tail, valueLength);
    _headerSize++;
    // XXX: size
  }

  //
  // attribute management
  //
  
  //
  // request parsing
  //
  
  @Override
  public Invocation parseInvocation()
    throws IOException
  {
    Objects.requireNonNull(request());
    
    // initialize state for a request
    //initRequest();

    if (! parseRequest()) {
      return null;
    }
    
    if (isUpgrade() && upgradeHttp2()) {
      return null;
    }
    
    CharSequence host = hostInvocation();

    return invocation(host, _uri, _uriLength);
  }
  
  long sequence()
  {
    return _sequence;
  }

  /**
   * Parses a http request.
   */
  private boolean parseRequest()
    throws IOException
  {
    try {
      ReadStream is = connTcp().readStream();
      
      if (! readRequest(is)) {
        clearRequest();

        return false;
      }
      
      _sequence = connHttp().nextSequenceRead();

      if (log.isLoggable(Level.FINE)) {
        log.fine(_method + " "
                 + new String(_uri, 0, _uriLength) + " " + _protocol
                 + " (" + dbgId() + ")");
        log.fine("Remote-IP: " + connTcp().addressRemote()
                 + ":" + connTcp().portRemote() + " (" + dbgId() + ")");
      }

      parseHeaders(is);

      return true;
    } catch (ClientDisconnectException e) {
      throw e;
    } catch (SocketTimeoutException e) {
      log.log(Level.FINER, e.toString(), e);
      
      return false;
    } catch (ArrayIndexOutOfBoundsException e) {
      log.log(Level.FINEST, e.toString(), e);
      
      throw new BadRequestException(L.l("Invalid request: URL or headers are too long"), e);
    } catch (Throwable e) {
      log.log(Level.FINEST, e.toString(), e);
      
      throw new BadRequestException(String.valueOf(e), e);
    }
  }

  /*
  public void onAccept()
  {
    startRequest();
  }
  */

  /**
   * Clear the request variables in preparation for a new request.
   *
   * @param s the read stream for the request
   */
  @Override
  protected void initRequest()
  {
    super.initRequest();
    
    _state = _state.toActive();
    
    // HttpBufferStore httpBuffer = getHttpBufferStore();
    HttpBufferStore bufferStore = getHttpBufferStore();

    _method.clear();
    _methodString = null;
    _protocol.clear();

    _uriLength = 0;
    if (bufferStore == null) {
      // server/05h8
      _uri = getSmallUriBuffer(); // httpBuffer.getUriBuffer();
      
      _headerBuffer = getSmallHeaderBuffer(); // httpBuffer.getHeaderBuffer();
      _headerKeys = getSmallHeaderKeys();     // httpBuffer.getHeaderKeys();
      _headerValues = getSmallHeaderValues(); // httpBuffer.getHeaderValues();
    }

    _uriHost.clear();
    _host = null;
    _keepalive = KeepaliveState.INIT;

    _headerSize = 0;
    _headerLength = 0;
    
    _inOffset = 0;
    _isChunkedIn = false;
    _isFirst = true;
  }

  /**
   * Read the first line of a request:
   *
   * GET [http://www.caucho.com[:80]]/path [HTTP/1.x]
   *
   * @return true if the request is valid
   */
  private boolean readRequest(ReadStream is)
    throws IOException
  {
    // server/12o3 - default to 1.0 for error messages in request
    _version = HTTP_1_0;
    
    boolean []isHttpWhitespace = _isHttpWhitespace;
    char []toUpperAscii = _toUpperAscii;
    
    byte []readBuffer = is.buffer();
    int readOffset = is.offset();
    int readLength = is.length();
    int ch;

    // skip leading whitespace
    do {
      if (readLength <= readOffset) {
        if ((readLength = is.fillBuffer()) < 0) {
          return false;
        }

        readOffset = 0;
      }

      ch = readBuffer[readOffset++] & 0xff;
    } while (isHttpWhitespace[ch]);

    char []buffer = _method.buffer();
    int length = buffer.length;
    int offset = 0;

    // scan method
    while (true) {
      if (length <= offset) {
      }
      else if (ch > ' ') {
        buffer[offset++] = toUpperAscii[ch];
      }
      else {
        break;
      }
        

      if (readLength <= readOffset) {
        if ((readLength = is.fillBuffer()) < 0)
          return false;

        readOffset = 0;
      }
      
      ch = readBuffer[readOffset++];
    }
    
    _method.length(offset);

    // skip whitespace
    while (ch == ' ' || ch == '\t') {
      if (readLength <= readOffset) {
        if ((readLength = is.fillBuffer()) < 0)
          return false;

        readOffset = 0;
      }

      ch = readBuffer[readOffset++];
    }
    
    if (ch == '*') {
      if (_method.matches("PRI") && startHttp2()) {
        return false;
      }
      else if (_method.matches("HAMP")) {
        if (! request().startBartender()) {
          return false;
        }
        else {
          log.warning("Invalid Request: unable to start HAMP");
          
          throw new BadRequestException("Invalid Request(Remote IP=" + connTcp().ipRemote() + ")");
        }
      }
    }

    byte []uriBuffer = _uri;
    int uriLength = 0;

    // skip 'http:'
    if (ch != '/') {
      while (ch > ' ' && ch != '/') {
        if (readLength <= readOffset) {
          if ((readLength = is.fillBuffer()) < 0)
            return false;
          readOffset = 0;
        }
        ch = readBuffer[readOffset++];
      }
      
      if (ch == '/') {
      }
      else {
        log.warning("Invalid Request (method='" + _method + "' url ch=0x" + Integer.toHexString(ch & 0xff)
                    + " off=" +readOffset + " len=" + readLength
                    + ") (IP=" + connTcp().ipRemote() + ")");
        log.warning(new String(readBuffer, 0, readLength));
        
        throw new BadRequestException("Invalid Request (Remote IP=" + connTcp().ipRemote() + ")");
      }

      if (readLength <= readOffset) {
        if ((readLength = is.fillBuffer()) < 0) {
          if (ch == '/') {
            uriBuffer[uriLength++] = (byte) ch;
            _uriLength = uriLength;
          }
          
          _version = 0;

          return true;
        }
        readOffset = 0;
      }

      int ch1 = readBuffer[readOffset++] & 0xff;

      if (ch1 != '/') {
        uriBuffer[uriLength++] = (byte) ch;
        ch = ch1;
      }
      else {
        // read host
        host:
        while (true) {
          if (readLength <= readOffset) {
            if ((readLength = is.fillBuffer()) < 0) {
              _version = 0;
              
              return true;
            }
            readOffset = 0;
          }
          ch = readBuffer[readOffset++] & 0xff;

          switch (ch) {
          case ' ': case '\t': case '\n': case '\r':
            break host;

          case '?':
            break host;

          case '/':
            break host;

          default:
            _uriHost.append((char) ch);
            break;
          }
        }
      }
    }
    
    int readTail = uriBuffer.length - uriLength - 1;
    
    readTail = Math.min(readTail, readLength);

    // read URI
    while (! isHttpWhitespace[ch]) {
      // There's no check for over-running the length because
      // allowing resizing would allow a DOS memory attack and
      // also lets us save a bit of efficiency.
      uriBuffer[uriLength++] = (byte) ch;

      if (readTail <= readOffset) {
        if ((readTail = fillUrlTail(is, readOffset, uriLength)) <= 0) {
          _uriLength = uriLength;
          _version = 0;
          return true;
        }

        readOffset = is.offset();
        uriBuffer = _uri;
        uriLength = _uriLength;
      }
      
      ch = readBuffer[readOffset++] & 0xff;
    }
    
    _uriLength = uriLength;
    _version = 0;

    // skip whitespace
    while (ch == ' ' || ch == '\t') {
      if (readLength <= readOffset) {
        readOffset = 0;
        if ((readLength = is.fillBuffer()) < 0)
          return true;
      }
      ch = readBuffer[readOffset++] & 0xff;
    }

    buffer = _protocol.buffer();
    length = buffer.length;
    offset = 0;
    
    // scan protocol
    while (! isHttpWhitespace[ch]) {
      if (offset < length) {
        buffer[offset++] = toUpperAscii[ch];
      }

      if (readLength <= readOffset) {
        readOffset = 0;
        if ((readLength = is.fillBuffer()) < 0) {
          _protocol.length(offset);
          return true;
        }
      }
      ch = readBuffer[readOffset++] & 0xff;
    }

    _protocol.length(offset);

    if (offset != 8) {
      _protocol.append("HTTP/0.9");
      _version = HTTP_0_9;
      killKeepalive("0.9");
    }
    else if (buffer[7] == '1') { // && _protocol.equals(_http11Cb))
      _version = HTTP_1_1;
    }
    else if (buffer[7] == '0') { // && _protocol.equals(_http10Cb))
      _version = HTTP_1_0;
    }
    else {
      _version = HTTP_0_9;
      killKeepalive("0.9");
    }

    // skip to end of line
    while (ch != '\n') {
      if (readLength <= readOffset) {
        if ((readLength = is.fillBuffer()) < 0)
          return true;
        readOffset = 0;
      }
      ch = readBuffer[readOffset++];
    }

    is.offset(readOffset);

    return true;
  }
  
  //
  // body reading
  //
  
  /*
  protected void setHmuxRequest(HmuxRequest hmuxRequest)
  {
    _hmuxRequest = hmuxRequest;
  }
  */
  
  @Override
  public boolean readBodyChunk()
    throws IOException
  {
    long inOffset = _inOffset;
    
    if (inOffset == 0) {
      if (! initBody()) {
        request().onBodyComplete();
        
        return false;
      }
    }
    
    if (_isChunkedIn) {
      return readChunk();
    }
    else {
      return readContentLength();
    }
  }
  
  private boolean readChunk()
    throws IOException
  {
    long inOffset = _inOffset;
    
    if (inOffset == 0) {
      inOffset = readChunkHeader();
    }
    
    if (inOffset < 0) {
      return false;
    }
    
    ReadStream is = connTcp().readStream();
    
    int sublen = (int) Math.min(Integer.MAX_VALUE, inOffset);
    sublen = Math.min(sublen, is.availableBuffer());
    
    if (sublen == 0) {
      return true;
    }
    
    if (sublen <= TempBuffer.SMALL_SIZE) {
      TempBuffer tBuf = TempBuffer.createSmall();
      
      int readlen = is.readAll(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset -= sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      
      return true;
    }
    else if (sublen <= TempBuffer.SIZE) {
      TempBuffer tBuf = TempBuffer.create();
      
      int readlen = is.readAll(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset -= sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      
      return true;
    }
    else {
      // XXX: should allow multiple reads -- although Buffer will fix
      TempBuffer tBuf = TempBuffer.create();
      
      int readlen = is.readAll(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset -= sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      // more data expected
      
      return true;
    }
  }
  
  private int readChunkHeader()
    throws IOException
  {
    ReadStream is = connTcp().readStream();
    int ch;
    
    if ((ch = is.read()) == '\r') {
      if ((ch = is.read()) == '\n') {
        ch = is.read();
      }
    }
    else if (ch == '\n') {
      ch = is.read();
    }
    
    int len = readChunkLen(is, ch);

    if (len <= 0) {
      request().onBodyComplete();
      len = -1;
    }
    
    _inOffset = len;
      
    return len;
  }
  
  private int readChunkLen(ReadStream is, int ch)
    throws IOException
  {
    int len = 0;
    
    while (true) {
      if ('0' <= ch && ch <= '9') {
        len = len * 16 + ch - '0';
      }
      else if ('a' <= ch && ch <= 'f') {
        len = len * 16 + ch - 'a' + 10;
      }
      else if ('A' <= ch && ch <= 'F') {
        len = len * 16 + ch - 'A' + 10;
      }
      else if (ch == '\r') {
        if ((ch = is.read()) == '\n') {
          return len;
        }
      }
      else if (ch == '\n') {
        return len;
      }
      else {
        throw new BadRequestException(L.l("Invalid chunk length"));
      }
      
      ch = is.read();
    }
  }
  
  private boolean readContentLength()
    throws IOException
  {
    long inOffset = _inOffset;
    
    long contentLength = contentLength();
    
    if (contentLength <= inOffset) {
      request().onBodyComplete();
      return false;
    }
    
    ReadStream is = connTcp().readStream();
    
    int sublen = (int) Math.min(Integer.MAX_VALUE, (contentLength - inOffset));
    sublen = Math.min(sublen, is.availableBuffer());
    
    if (sublen == 0) {
      return true;
    }
    
    if (sublen <= TempBuffer.SMALL_SIZE) {
      TempBuffer tBuf = TempBuffer.createSmall();
      
      int readlen = is.read(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset += sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      
      if (contentLength <= _inOffset) {
        request().onBodyComplete();
      }
      
      return false;
    }
    else if (sublen <= TempBuffer.SIZE) {
      TempBuffer tBuf = TempBuffer.create();
      
      int readlen = is.read(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset += sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      
      if (contentLength <= _inOffset) {
        request().onBodyComplete();
      }
      
      return false;
    }
    else {
      // XXX: should allow multiple reads -- although Buffer will fix
      TempBuffer tBuf = TempBuffer.create();
      
      int readlen = is.read(tBuf.buffer(), 0, sublen);
      
      if (readlen != sublen) {
        throw new IllegalStateException();
      }
      
      _inOffset += sublen;
      
      tBuf.length(sublen);
      request().onBodyChunk(tBuf);
      // more data expected
      
      return true;
    }
  }
  
  private boolean initBody()
    throws IOException
  {
    // _hmuxRequest = null;
    
    long contentLength = contentLength();

    if (contentLength < 0 && HTTP_1_1 <= version()
        && header("Transfer-Encoding") != null) {
      _isChunkedIn = true;
      
      return true;
    }
    // Otherwise use content-length
    else if (contentLength >= 0) {
      return true;
    }
    else if (method().equals("POST")) {
      _inOffset = Long.MAX_VALUE;
      
      throw new BadRequestException("POST requires content-length");
    }
    else {
      _inOffset = Long.MAX_VALUE;

      return false;
    }
  }
  
  //
  // upgrades
  //
  
  protected boolean startHmux()
  {
    return false;
  }
  
  private boolean upgradeHttp2() throws IOException
  {
    String connHeader = header("Connection");
    String upgradeHeader = header("Upgrade");
    
    if (! "h2-12".equals(upgradeHeader)
        || connHeader.indexOf("HTTP2-Settings") < 0) {
      return false;
    }
    
    String settings = header("HTTP2-Settings");
    
    if (settings == null) {
      return false;
    }
    
    ConnectionHttp2 handler;
    
    handler = new ConnectionHttp2(protocolHttp(), 
                                     http(),
                                     connTcp());
    
    startDuplex(handler);
    
    handler.onStartUpgrade(this);
    
    return true;
  }
  
  private boolean startHttp2()
    throws IOException
  {
    ReadStream is = connTcp().readStream();

    //int offset = is.getOffset();
    
    byte []header = new byte[24];
    
    if (is.readAll(header, 0, header.length) != header.length) {
      // XXX: rewind
      return false;
    }
    
    if (! Arrays.equals(header, Http2Constants.CONNECTION_HEADER)) {
      return false;
    }
    
    ConnectionHttp2 handler;
    
    handler = new ConnectionHttp2(protocolHttp(), 
                                     http(),
                                     connTcp());
    
    startDuplex(handler);
    
    handler.onStart();
    
    return true;
  }

  /**
   * Parses headers from the read stream.
   *
   * @param s the input read stream
   */
  private void parseHeaders(ReadStream s) throws IOException
  {
    int version = version();

    if (version < HTTP_1_0) {
      return;
    }

    if (version < HTTP_1_1) {
      killKeepalive("http client version less than 1.1: " + version);
    }

    byte []readBuffer = s.buffer();
    int readOffset = s.offset();
    int readLength = s.length();

    char []headerBuffer = _headerBuffer;
    headerBuffer[0] = 'z';
    int headerOffset = 1;
    _headerSize = 0;
    
    int readTail = readLength;
    if (headerBuffer.length - 1 < readTail - readOffset) {
      readTail = headerBuffer.length - 1;
    }

    boolean isLogFine = log.isLoggable(Level.FINE);

    while (true) {
      int ch;

      int keyOffset = headerOffset;

      // scan the key
      while (true) {
        if (readTail <= readOffset) {
          if ((readTail = fillHeaderTail(s, readOffset, headerOffset)) <= 0) {
            return;
          }

          readOffset = s.offset();
          headerOffset = _headerLength;
          headerBuffer = _headerBuffer;
        }

        ch = readBuffer[readOffset++];

        if (ch == '\n') {
          s.offset(readOffset);
          return;
        }
        else if (ch == ':') {
          break;
        }

        headerBuffer[headerOffset++] = (char) ch;
      }

      // strip trailing whitespace from key
      while (headerBuffer[headerOffset - 1] == ' ') {
        headerOffset--;
      }
      
      int keyLength = headerOffset - keyOffset;

      // skip whitespace
      do {
        if (readTail <= readOffset) {
          if ((readTail = fillHeaderTail(s, readOffset, headerOffset)) <= 0) {
            return;
          }

          readOffset = s.offset();
          headerOffset = _headerLength;
          headerBuffer = _headerBuffer;
        }
        
        ch = readBuffer[readOffset++];
      } while (ch == ' ' || ch == '\t');

      int valueOffset = headerOffset;

      // scan the value
      while (true) {
        if (readTail <= readOffset) {
          if ((readTail = fillHeaderTail(s, readOffset, headerOffset)) <= 0) {
            break;
          }

          readOffset = s.offset();
          headerOffset = _headerLength;
          headerBuffer = _headerBuffer;
        }

        if (ch == '\n') {
          int ch1 = readBuffer[readOffset];

          if (ch1 == ' ' || ch1 == '\t') {
            ch = ' ';
            readOffset++;

            if (headerBuffer[headerOffset - 1] == '\r') {
              headerOffset--;
            }
          }
          else
            break;
        }

        headerBuffer[headerOffset++] = (char) ch;

        ch = readBuffer[readOffset++];
      }

      while (headerBuffer[headerOffset - 1] <= ' ') {
        headerOffset--;
      }
      
      int headerSize = _headerSize;
      
      CharSegment []headerKeys = _headerKeys;
      CharSegment []headerValues = _headerValues;

      if (headerKeys.length <= headerSize) {
        _headerLength = headerOffset;
        extendHeaderBuffers();
        
        headerBuffer = _headerBuffer;
        headerKeys = _headerKeys;
        headerValues = _headerValues;
      }

      headerKeys[headerSize].init(headerBuffer, keyOffset, keyLength);

      int valueLength = headerOffset - valueOffset;
      headerValues[headerSize].init(headerBuffer, valueOffset, valueLength);

      if (isLogFine) {
        log.fine(headerKeys[headerSize] + ": " + headerValues[headerSize]
                 + " (" + dbgId() + ")");
      }

      if (addHeaderInt(headerBuffer, keyOffset, keyLength,
                       headerValues[headerSize])) {
        headerSize++;
      }

      _headerSize = headerSize;
    }
  }
  
  private int fillUrlTail(ReadStream s, int readOffset,
                          int uriOffset)
    throws IOException
  {
    _uriLength = uriOffset;
    
    if (_uri.length <= uriOffset) {
      extendHeaderBuffers();
    }
    
    if (s.length() <= readOffset) {
      if (s.fillBuffer() < 0) {
        return -1;
      }
    }
    else {
      s.offset(readOffset);
    }
    
    int tail = s.length() - s.offset();
    
    if (_uri.length - uriOffset < tail) {
      tail = _uri.length - uriOffset;
    }
    
    return tail;
  }
  
  private int fillHeaderTail(ReadStream s, int readOffset,
                             int headerOffset)
    throws IOException
  {
    _headerLength = headerOffset;
    
    if (_headerBuffer.length <= headerOffset) {
      extendHeaderBuffers();
    }
    
    if (s.length() <= readOffset) {
      if (s.fillBuffer() < 0) {
        return -1;
      }
    }
    else {
      s.offset(readOffset);
    }
    
    int tail = s.length() - s.offset();
    
    if (_headerBuffer.length - headerOffset < tail) {
      tail = _headerBuffer.length - headerOffset;
    }
    
    return tail;
  }

  protected void extendHeaderBuffers()
    throws IOException
  {
    HttpBufferStore bufferStore = getHttpBufferStore();
    
    if (bufferStore != null) {
      throw new BadRequestException(L.l("URL or HTTP headers are too long (IP={0})",
                                        connTcp().ipRemote()));
    }
    
    bufferStore = allocateHttpBufferStore();
    
    byte []uri = bufferStore.getUriBuffer();
    System.arraycopy(_uri,  0, uri, 0, _uriLength);
    
    char []headerBuffer = bufferStore.getHeaderBuffer();
    CharSegment []headerKeys = bufferStore.getHeaderKeys();
    CharSegment []headerValues = bufferStore.getHeaderValues();
    
    if (headerBuffer == _headerBuffer || _uri == uri) {
      throw new IllegalStateException();
    }
    
    System.arraycopy(_headerBuffer, 0, headerBuffer, 0, _headerLength);
    
    for (int i = 0; i < _headerSize; i++) {
      headerKeys[i].init(headerBuffer,  
                         _headerKeys[i].offset(),
                         _headerKeys[i].length());
      
      headerValues[i].init(headerBuffer,  
                           _headerValues[i].offset(),
                           _headerValues[i].length());
    }
    
    _uri = uri;
    _headerBuffer = headerBuffer;
    _headerKeys = headerKeys;
    _headerValues = headerValues;
  }
  
  @Override
  public void killKeepalive(String msg)
  {
    super.killKeepalive(msg);
    
    if (_keepalive == KeepaliveState.ALLOC) {
      // XXX: free
    }
    
    _keepalive = KeepaliveState.KILL;
  }
  
  /*
  @Override
  public boolean isKeepalive()
  {
    switch (_keepalive) {
    case KILL:
      return false;
      
    case ALLOC:
      return true;
      
    case INIT:
      // XXX: try to allocate
      return true;
      
    default:
      return false;
    }
  }*/

  /**
   * Handles a timeout.
   */
  @Override
  public void onTimeout()
  {
    try {
      //request().sendError(WebRequest.INTERNAL_SERVER_ERROR);
      if (true) throw new UnsupportedOperationException();
    } catch (Exception e) {
      log.log(Level.FINEST, e.toString(), e);
    }
    
    closeResponse();
  }
  
  public void onCloseRead()
  {
  }
  
  @Override
  public void onCloseConnection()
  {
    super.onCloseConnection();
    
    _headerBuffer = null;
    _headerKeys = null;
    _headerValues = null;
  }

  //
  // upgrade to duplex
  //

  /*
  @Override
  public boolean isDuplex()
  {
    throw new UnsupportedOperationException();
    //return getRequestProtocol().isDuplex();
  }
  */
  
  @Override
  public void startDuplex(ConnectionProtocol request)
  {
    connHttp().request(request);
  }

  /**
   * Cleans up at the end of the invocation
   */

  /*
  //@Override
  public void finishRequest()
    throws IOException
  {
    //super.finishRequest();

    skip();
  }
  */

  @Override
  protected String dbgId()
  {
    HttpContainer httpSystem = http();
    
    String serverId = "";
    
    if (httpSystem != null) {
      serverId = httpSystem.getServerDisplayName();
    }
    
    long connId = connectionId();

    if ("".equals(serverId))
      return "Http[" + connId + "] ";
    else
      return "Http[" + serverId + ", " + connId + "] ";
  }

  public String getDebugId()
  {
    HttpContainer httpSystem = http();
    
    String serverId = "";
    
    if (httpSystem != null) {
      serverId = httpSystem.getServerDisplayName();
    }
    
    long connId = connectionId();

    if ("".equals(serverId))
      return "" + connId + "";
    else
      return "" + serverId + ", " + connId + "";
  }

  @Override
  protected OutHttpApp createOut()
  {
    //RequestHttp request = (RequestHttp) getRequest();

    return new OutHttpApp1(this);
  }
  
  boolean isChunked()
  {
    return out().isChunked();
  }

  public boolean calculateChunkedEncoding()
  {
    //RequestFacade request = request();
    
    if (RequestHttp1.HTTP_1_1 <= version()
        && contentLengthOut() < 0
        && ! method().equalsIgnoreCase("HEAD")) {
      return true;
    }
    else {
      return false;
    }
  }
  
  @Override
  public boolean canWrite(long writeSequence)
  {
    return sequence() <= writeSequence;
  }
  
  @Override
  public boolean write(WriteStream out, 
                       Buffer data,
                       boolean isEnd)
  {
    writeImpl(out, data, isEnd);
    
    return ! isKeepalive();
  }
  
  private void writeImpl(WriteStream out, 
                            Buffer data,
                            boolean isEnd)
  {
    try {
      if (_isFirst) {
        _isFirst = false;
        
        int length = data != null ? data.length() : 0;
        
        if (isEnd) {
          writeHeaders(out, length);
        }
        else {
          writeHeaders(out, -1);
        }
        
        if (isChunked() && length > 0) {
          writeFirstChunk(out, length);
        }
      }

      if (data != null) {
        out.write(data);
        data.free();
      }
      
      if (isEnd) {
        if (isChunked()) {
          writeLastChunk(out);
        }
        
        closeWrite();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      log.log(Level.WARNING, e.toString(), e);
      
      disconnect(out);
    }
  }

  @Override
  public void closeWrite()
  {
    super.closeWrite();
    
    _state = _state.toFree();
    
    protocolHttp().requestFree(this);
    
    //state.onCloseWrite();
  }

  private void saveFirst(WriteStream out,
                         Buffer data,
                         boolean isEnd)
  {
    _pending = new PendingFirst(out, data, isEnd);
  }
  
  @Override
  public void writePending()
  {
    PendingFirst pending = _pending;
    _pending = null;
    
    if (pending != null) {
      pending.write();
    }
  }
  
  abstract private class Pending
  {
    private Pending _next;
    
    void next(Pending next)
    {
      if (_next != null) {
        _next.next(next);
      }
      else {
        _next = next;
      }
    }
    
    void writeNext()
    {
      Pending next = _next;
      
      if (next != null) {
        next.write();
      }
    }
    
    abstract void write(); 
  }
  
  private class PendingFirst extends Pending
  {
    private WriteStream _out;
    private Buffer _data;
    private boolean _isEnd;
    
    PendingFirst(WriteStream out,
                 Buffer data,
                 boolean isEnd)
    {
      _out = out;
      _data = data;
      _isEnd = isEnd;
    }
    
    @Override
    void write()
    {
      writeImpl(_out, _data, _isEnd);
      writeNext();
    }
  }
  
  private void writeFirstChunk(WriteStream os, long length)
    throws IOException
  {
    os.write('\r');
    os.write('\n');
    os.write(toHex(length >> 12));
    os.write(toHex(length >> 8));
    os.write(toHex(length >> 4));
    os.write(toHex(length >> 0));
    os.write('\r');
    os.write('\n');
  }
  
  private void writeLastChunk(WriteStream os)
    throws IOException
  {
    os.write('\r');
    os.write('\n');
    os.write('0');
    os.write('\r');
    os.write('\n');
    os.write('\r');
    os.write('\n');
  }
  
  private static int toHex(long v)
  {
    v = v & 0xf;
    
    if (v < 0xa) {
      return (int) (v + '0');
    }
    else {
      return (int) (v - 10 + 'a');
    }
  }
  
  @Override
  public void upgrade()
  {
    if (isOutCommitted()) {
      throw new IllegalStateException();
      
    }
    
    _isUpgrade = true;
    
    out().upgrade();
  }

  /**
   * Writes the 100 continue response.
   */
  @Override
  protected void writeContinueInt()
    throws IOException
  {
    if (true) throw new UnsupportedOperationException();

    /*
    WriteStream os = getRawWrite();
    os.print("HTTP/1.1 100 Continue\r\n\r\n");
    os.flush();
    */
  }

  /**
   * Implementation to write the HTTP headers.  If the length is positive,
   * it's a small request where the buffer contains the entire request,
   * so the length is already known.
   *
   * @param os the output stream to write the headers to.
   * @param length if non-negative, the length of the entire request.
   *
   * @return true if the data in the request should use chunked encoding.
   */
  //@Override
  private void writeHeaders(WriteStream os, long length)
    throws IOException
  {
    int version = version();
    boolean debug = log.isLoggable(Level.FINE);

    if (version < RequestHttp1.HTTP_1_0) {
      killKeepalive("http client version " + version);
      return;
    }
    
    long contentLength = contentLengthOut();

    int statusCode = status();
    
    if (statusCode == 200) {
      if (version < RequestHttp1.HTTP_1_1) {
        os.write(_http10ok, 0, _http10ok.length);
      }
      else {
        os.write(_http11ok, 0, _http11ok.length);
      }
    }
    else {
      if (version < RequestHttp1.HTTP_1_1) {
        os.printLatin1("HTTP/1.0 ");
      }
      else {
        os.printLatin1("HTTP/1.1 ");
      }

      os.write((statusCode / 100) % 10 + '0');
      os.write((statusCode / 10) % 10 + '0');
      os.write(statusCode % 10 + '0');
      os.write(' ');
      os.printLatin1(statusMessage());
    }

    //String contentType = contentType();

    if (debug) {
      log.fine("HTTP/1.1 " +
               statusCode + " " + statusMessage() + " (" + dbgId() + ")");
    }

    if (_isUpgrade) {
      String upgrade = headerOut("Upgrade");

      if (upgrade != null) {
        os.printLatin1("\r\nUpgrade: ");
        os.printLatin1NoLf(upgrade);
      }

      os.printLatin1("\r\nConnection: Upgrade");
      killKeepalive("duplex/upgrade");

      if (debug) {
        log.fine(dbgId() + "Connection: Upgrade");
      }
    }

    // asdf
    String serverHeader = serverHeader();
    if (serverHeader == null) {
      os.write(_serverHeaderBytes, 0, _serverHeaderBytes.length);
    }
    else {
      os.printLatin1("\r\nserver: ");
      os.printLatin1NoLf(serverHeader);
    }

    ArrayList<String> headerKeys = headerKeysOut();
    ArrayList<String> headerValues = headerValuesOut();
    int size = headerKeys.size();

    for (int i = 0; i < size; i++) {
      String key = headerKeys.get(i);

      if (_isUpgrade && "Upgrade".equalsIgnoreCase(key)) {
        continue;
      }

      os.write('\r');
      os.write('\n');
      os.printLatin1NoLf(key);
      os.write(':');
      os.write(' ');
      os.printLatin1NoLf(headerValues.get(i));

      if (debug) {
        log.fine(key + ": " + headerValues.get(i) + " (" + dbgId() + ")");
      }
    }
    
    String contentType = headerOutContentType();
    String contentEncoding = headerOutContentEncoding();
    
    if (contentType != null) {
      os.write(_contentTypeBytes);
      os.printLatin1NoLf(contentType);
      
      if (contentEncoding != null) {
        os.write(_charsetBytes);
        os.printLatin1NoLf(contentEncoding);
      }
    }

    writeCookies(os);

    if (hasFooter()) {
      contentLength = -1;
      length = -1;
    }

    if (contentLength >= 0) {
      os.write(_contentLengthBytes, 0, _contentLengthBytes.length);
      os.print(contentLength);
      //hasContentLength = true;

      if (debug) {
        log.fine("Content-Length: " + contentLength + " (" + dbgId() + ")");
      }
    }
    else if (statusCode == HttpConstants.SC_NOT_MODIFIED) {
      // #3089
      // In the HTTP spec, a 304 has no message body so the content-length
      // is not needed.  The content-length is not explicitly forbidden,
      // but does cause problems with certain clients.
      //hasContentLength = true;
      setHead();
    }
    else if (statusCode == HttpConstants.SC_NO_CONTENT) {
      //hasContentLength = true;
      os.write(_contentLengthBytes, 0, _contentLengthBytes.length);
      os.print(0);
      setHead();

      if (debug)
        log.fine("Content-Length: 0 (" + dbgId() + ")");
    }
    else if (length >= 0) {
      os.write(_contentLengthBytes, 0, _contentLengthBytes.length);
      os.print(length);
      //hasContentLength = true;

      if (debug) {
        log.fine("Content-Length: " + length + " (" + dbgId() + ")");
      }
    }

    if (version < RequestHttp1.HTTP_1_1) {
      killKeepalive("http response version: " + version);
    }
    else {
      if (isKeepalive()) {
      }
      else if (_isUpgrade) {
        killKeepalive("http response upgrade");
      }
      else {
        os.write(_connectionCloseBytes, 0, _connectionCloseBytes.length);

        if (debug)
          log.fine(dbgId() + "Connection: close");
      }
    }
    
    long now = CurrentTime.currentTime();
    byte []dateBuffer = fillDateBuffer(now);
    int dateBufferLength = getDateBufferLength();

    if (isChunked()) {
      if (debug) {
        log.fine(dbgId() + "Transfer-Encoding: chunked");
      }

      os.write(_transferChunkedBytes);
      os.write(dateBuffer, 0, dateBufferLength - 2);
    }
    else {
      os.write(dateBuffer, 0, dateBufferLength);
    }
  }

  private void writeCookies(WriteStream os) throws IOException
  {
    ArrayList<CookieWeb> cookiesOut = cookiesOut();

    if (cookiesOut == null) {
      return;
    }
    
    int size = cookiesOut.size();
    
    for (int i = 0; i < size; i++) {
      CookieWeb cookie = cookiesOut.get(i);

      printCookie(os, cookie);
    }
  }
  
  private void printCookie(WriteStream os, CookieWeb cookie)
    throws IOException
  {
    os.print("\r\nset-cookie: ");
    os.print(cookie.name());
    os.print("=");
    os.print(cookie.value());
    
    if (cookie.domain() != null) {
      os.print("; Domain=");
      os.print(cookie.domain());
    }
    
    if (cookie.path() != null) {
      os.print("; Path=");
      os.print(cookie.path());
    }
    
    if (cookie.maxAge() >= 0) {
      os.print("; MaxAge=");
      os.print(cookie.maxAge() / 1000);
    }
    
    if (cookie.httpOnly()) {
      os.print("; HttpOnly");
    }
    
    if (cookie.secure()) {
      os.print("; Secure");
    }
  }

  @Override
  public String toString()
  {
    HttpContainer httpSystem = http();
    
    String serverId;
    
    if (httpSystem != null)
      serverId = httpSystem.getServerDisplayName();
    else {
      serverId = "server";
    }
    
    long connId = connectionId();

    if ("".equals(serverId))
      return getClass().getSimpleName() + "[" + connId + "]";
    else {
      return getClass().getSimpleName() + ("[" + serverId + ", " + connId + "]");
    }
  }
  
  enum KeepaliveState {
    INIT,
    ALLOC,
    KILL;
  }
  
  enum StateAlloc {
    NEW {
      
    },
    ACTIVE {
      StateAlloc toActive()
      {
        System.out.println("TAD: " + this);
        return ACTIVE;
      }

      @Override
      StateAlloc toFree() { return FREE; }
    },
    FREE;
    
    StateAlloc toActive()
    {
      return ACTIVE;
    }
    
    StateAlloc toFree()
    {
      System.out.println("TO-FREE: " + this);
      return FREE;
    }
  }
  
  static {
    _toLowerAscii = new char[256];
    
    for (int i = 0; i < 256; i++) {
      if ('A' <= i && i <= 'Z') {
        _toLowerAscii[i] = (char) (i + 'a' - 'A');
      }
      else {
        _toLowerAscii[i] = (char) i;
      }
    }
    
    _toUpperAscii = new char[256];
    
    for (int i = 0; i < 256; i++) {
      if ('a' <= i && i <= 'z') {
        _toUpperAscii[i] = (char) (i + 'A' - 'a');
      }
      else {
        _toUpperAscii[i] = (char) i;
      }
    }
    
    _isHttpWhitespace = new boolean[256];
    _isHttpWhitespace[' '] = true;
    _isHttpWhitespace['\t'] = true;
    _isHttpWhitespace['\r'] = true;
    _isHttpWhitespace['\n'] = true;
  }
}
