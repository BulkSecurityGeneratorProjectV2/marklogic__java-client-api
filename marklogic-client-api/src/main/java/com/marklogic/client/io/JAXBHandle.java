/*
 * Copyright (c) 2019 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.marklogic.client.io.marker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marklogic.client.MarkLogicIOException;

/**
 * A JAXB Handle roundtrips a POJO (a Java data structure) to and from a database document.
 *
 * The POJO class must have JAXB annotations or must be generated by xjc from an XML Schema.
 *
 * The JAXB Handle must be initialized with a JAXB Context with which the root POJO classes
 * have been registered.
 *
 * Important:  Do not use the JAXB unmarshaller to parse XML from an untrusted source
 * as it is vulnerable to XXE attacks.
 *
 * @param	<C>	one of the classes (or the &lt;?&gt; wildcard for any of the classes) registered with the JAXB Context
 */
public class JAXBHandle<C>
  extends BaseHandle<InputStream, OutputStreamSender>
  implements ResendableContentHandle<C, InputStream>, OutputStreamSender,
    XMLReadHandle, XMLWriteHandle
{
  static final private Logger logger = LoggerFactory.getLogger(JAXBHandle.class);

  private final JAXBContext context;
  private final Class<C>    contentClass;

  private Unmarshaller unmarshaller;
  private Marshaller   marshaller;
  private C            content;

  /**
   * Creates a factory to create a JAXBHandle instance for POJO instances
   * of the specified classes.
   * @param pojoClasses	the POJO classes for which this factory provides a handle
   * @return	the factory
   * @throws JAXBException if a JAXB error occurs while initializing the new factory
   */
  static public ContentHandleFactory newFactory(Class<?>... pojoClasses)
    throws JAXBException {
    if (pojoClasses == null || pojoClasses.length == 0)
      return null;
    return new JAXBHandleFactory(pojoClasses);
  }
  /**
   * Creates a factory to create a JAXBHandle instance for POJO instances
   * of the specified classes.
   * @param context	the JAXB context for marshaling the POJO classes
   * @param pojoClasses	the POJO classes for which this factory provides a handle
   * @return	the factory
   */
  static public ContentHandleFactory newFactory(JAXBContext context, Class<?>... pojoClasses) {
    if (context == null || pojoClasses == null || pojoClasses.length == 0)
      return null;
    return new JAXBHandleFactory(context, pojoClasses);
  }

  /**
   * Initializes the JAXB handle with the JAXB context for the classes
   * of the marshalled or unmarshalled structure.
   * @param context	the JAXB context
   */
  public JAXBHandle(JAXBContext context) {
    this(context, null);
  }

  /**
   * Initializes the JAXB handle with the JAXB context for the classes
   * of the marshalled or unmarshalled structure.
   * @param context	the JAXB context
   * @param contentClass the class of the content
   */
  public JAXBHandle(JAXBContext context, Class<C> contentClass) {
    super();
    setResendable(true);
    if (context == null) {
      throw new IllegalArgumentException(
        "null JAXB context for converting classes"
      );
    }
    super.setFormat(Format.XML);
    this.context = context;
    this.contentClass = contentClass;
  }

  /**
   * Returns the root object of the JAXB structure for the content.
   * @return	the root JAXB object
   */
  @Override
  public C get() {
    return content;
  }
  /**
   * Returns the root object of the JAXB structure for the content
   * cast to a more specific class.
   * @param as	the class of the object
   * @param <T> the type to return
   * @return	the root JAXB object
   */
  public <T> T get(Class<T> as) {
    if (content == null) {
      return null;
    }
    if (as == null) {
      throw new IllegalArgumentException("Cannot cast content to null class");
    }
    if (!as.isAssignableFrom(content.getClass())) {
      throw new IllegalArgumentException(
        "Cannot cast "+content.getClass().getName()+" to "+as.getName()
      );
    }
    @SuppressWarnings("unchecked")
    T content = (T) get();
    return content;
  }
  /**
   * Assigns the root object of the JAXB structure for the content.
   * @param content	the root JAXB object
   */
  @Override
  public void set(C content) {
    this.content = content;
  }
  /**
   * Assigns the root object of the JAXB structure for the content
   * and returns the handle as a fluent convenience.
   * @param content	the root JAXB object
   * @return	this handle
   */
  public JAXBHandle<C> with(C content) {
    set(content);
    return this;
  }

  @Override
  public Class<C> getContentClass() {
    if (contentClass != null)
      return contentClass;
    if (content != null)
      return (Class<C>) content.getClass();
    return null;
  }
  @Override
  public JAXBHandle<C> newHandle() {
    return new JAXBHandle<>(context, getContentClass()).withMimetype(getMimetype());
  }
  @Override
  public JAXBHandle<C>[] newHandleArray(int length) {
    if (length < 0) throw new IllegalArgumentException("array length less than zero: "+length);
    return new JAXBHandle[length];
  }

  /**
   * Restricts the format to XML.
   */
  @Override
  public void setFormat(Format format) {
    if (format != Format.XML)
      throw new IllegalArgumentException("JAXBHandle supports the XML format only");
  }
  /**
   * Specifies the mime type of the content and returns the handle
   * as a fluent convenience.
   * @param mimetype	the mime type of the content
   * @return	this handle
   */
  public JAXBHandle<C> withMimetype(String mimetype) {
    setMimetype(mimetype);
    return this;
  }

  /**
   * fromBuffer() unmarshals a JAXB POJO from a byte array
   * buffer.  The buffer must store the marshaled XML for the
   * JAXB POJO in UTF-8 encoding. JAXB cannot unmarshal arbitrary XML.
   */
  @Override
  public void fromBuffer(byte[] buffer) {
    set(bytesToContent(buffer));
  }
  @Override
  public byte[] toBuffer() {
    return contentToBytes(get());
  }
  @Override
  public C bytesToContent(byte[] buffer) {
    return (buffer == null || buffer.length == 0) ?
            null : toContent(new ByteArrayInputStream(buffer));
  }
  @Override
  public byte[] contentToBytes(C content) {
    try {
      if (content == null)
        return null;

      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      write(buffer);

      return buffer.toByteArray();
    } catch (IOException e) {
      throw new MarkLogicIOException(e);
    }
  }
  @Override
  public C toContent(InputStream serialization) {
    if (serialization == null) return null;

    try {
      @SuppressWarnings("unchecked")
      C unmarshalled = (C) getUnmarshaller().unmarshal(
              new InputStreamReader(serialization, StandardCharsets.UTF_8)
      );
      return unmarshalled;
    } catch (JAXBException e) {
      logger.error("Failed to unmarshall object read from database document",e);
      throw new MarkLogicIOException(e);
    } finally {
      try {
        serialization.close();
      } catch (IOException e) {
        // ignore.
      }
    }
  }

  /**
   * Returns the JAXB structure as an XML string.
   */
  @Override
  public String toString() {
    byte[] buffer = toBuffer();
    return (buffer == null) ? null : new String(buffer, StandardCharsets.UTF_8);
  }

  /**
   * Returns the unmarshaller that converts a tree data structure
   * from XML to Java objects, reusing any existing unmarshaller.
   * @return	the unmarshaller for the JAXB context
   * @throws JAXBException if unmarshaller initialization fails
   */
  public Unmarshaller getUnmarshaller()
    throws JAXBException {
    return getUnmarshaller(true);
  }
  /**
   * Returns the unmarshaller that converts a tree data structure
   * from XML to Java objects.
   * @param reuse	whether to reuse an existing unmarshaller
   * @return	the unmarshaller for the JAXB context
   * @throws JAXBException if unmarshaller initialization fails
   */
  public Unmarshaller getUnmarshaller(boolean reuse)
    throws JAXBException {
    if (!reuse || unmarshaller == null) {
      unmarshaller = context.createUnmarshaller();
    }
    return unmarshaller;
  }
  /**
   * Returns the marshaller that converts a tree data structure
   * from Java objects to XML, reusing any existing marshaller.
   * @return	the marshaller for the JAXB context
   * @throws JAXBException if marshaller initialization fails
   */
  public Marshaller getMarshaller()
    throws JAXBException {
    return getMarshaller(true);
  }
  /**
   * Returns the marshaller that converts a tree data structure
   * from Java objects to XML.
   * @param reuse	whether to reuse an existing marshaller
   * @return	the marshaller for the JAXB context
   * @throws JAXBException if marshaller initialization fails
   */
  public Marshaller getMarshaller(boolean reuse)
    throws JAXBException {
    if (!reuse || this.marshaller == null) {
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING,         "UTF-8");
      this.marshaller = marshaller;
    }
    return this.marshaller;
  }

  @Override
  protected Class<InputStream> receiveAs() {
    return InputStream.class;
  }
  @Override
  protected void receiveContent(InputStream serialization) {
    set(toContent(serialization));
  }
  @Override
  protected OutputStreamSender sendContent() {
    try {
      return new OutputStreamSenderImpl<>(getMarshaller(), get());
    } catch (JAXBException e) {
      logger.error("Failed to construct marshaller for output stream sender",e);
      throw new MarkLogicIOException(e);
    }
  }

  @Override
  public void write(OutputStream out) throws IOException {
    sendContent().write(out);
  }

  static private class OutputStreamSenderImpl<C> implements OutputStreamSender {
    private final Marshaller marshaller;
    private final C content;
    private OutputStreamSenderImpl(Marshaller marshaller, C content) {
      if (content == null) {
        throw new IllegalStateException("No object to write");
      }
      this.marshaller = marshaller;
      this.content = content;
    }
    @Override
    public void write(OutputStream out) throws IOException {
      try {
        marshaller.marshal(content, out);
      } catch (JAXBException e) {
        logger.error("Failed to marshall object for writing to database document",e);
        throw new MarkLogicIOException(e);
      }
    }
  }

  static private class JAXBHandleFactory implements ContentHandleFactory {
    private final Class<?>[]    pojoClasses;
    private final JAXBContext   factoryContext;
    private final Set<Class<?>> classSet;

    private JAXBHandleFactory(Class<?>... pojoClasses)
      throws JAXBException {
      this(JAXBContext.newInstance(pojoClasses), pojoClasses);
    }
    private JAXBHandleFactory(JAXBContext factoryContext, Class<?>... pojoClasses) {
      super();
      this.pojoClasses    = pojoClasses;
      this.factoryContext = factoryContext;
      this.classSet       = new HashSet<>(Arrays.asList(pojoClasses));
    }

    @Override
    public Class<?>[] getHandledClasses() {
      return pojoClasses;
    }
    @Override
    public boolean isHandled(Class<?> type) {
      return classSet.contains(type);
    }
    @Override
    public <C> ContentHandle<C> newHandle(Class<C> type) {
      return isHandled(type) ? new JAXBHandle<>(factoryContext, type) : null;
    }
  }
}
