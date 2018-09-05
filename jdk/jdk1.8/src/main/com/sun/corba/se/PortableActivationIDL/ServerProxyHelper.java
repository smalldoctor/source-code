package com.sun.corba.se.PortableActivationIDL;


/**
* com/sun/corba/se/PortableActivationIDL/ServerProxyHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u141/9370/corba/src/share/classes/com/sun/corba/se/PortableActivationIDL/activation.idl
* Wednesday, July 12, 2017 4:36:58 AM PDT
*/


/** Server callback interface, passed to Activator in registerServer method.
    */
abstract public class ServerProxyHelper
{
  private static String  _id = "IDL:PortableActivationIDL/ServerProxy:1.0";

  public static void insert (org.omg.CORBA.Any a, com.sun.corba.se.PortableActivationIDL.ServerProxy that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static com.sun.corba.se.PortableActivationIDL.ServerProxy extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (com.sun.corba.se.PortableActivationIDL.ServerProxyHelper.id (), "ServerProxy");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static com.sun.corba.se.PortableActivationIDL.ServerProxy read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ServerProxyStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.corba.se.PortableActivationIDL.ServerProxy value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static com.sun.corba.se.PortableActivationIDL.ServerProxy narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof com.sun.corba.se.PortableActivationIDL.ServerProxy)
      return (com.sun.corba.se.PortableActivationIDL.ServerProxy)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      com.sun.corba.se.PortableActivationIDL._ServerProxyStub stub = new com.sun.corba.se.PortableActivationIDL._ServerProxyStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static com.sun.corba.se.PortableActivationIDL.ServerProxy unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof com.sun.corba.se.PortableActivationIDL.ServerProxy)
      return (com.sun.corba.se.PortableActivationIDL.ServerProxy)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      com.sun.corba.se.PortableActivationIDL._ServerProxyStub stub = new com.sun.corba.se.PortableActivationIDL._ServerProxyStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
