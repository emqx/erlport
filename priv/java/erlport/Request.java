package erlport;

import java.util.*;
import java.lang.*;
import java.nio.*;


import erlport.terms.*;

enum RequestType {
    CALL ("C"),    // 'C'
    MESSAGE ("M");    // 'M'

    private String value;

    private RequestType(String x) {
        this.value = x;
    }
}

// Request structure for erlport
public class Request extends Object {

    // ----- ParseState
    private Integer __pos;
    private byte[]  __bytes;
    Object __rawTerm;
    // ----- End

    // ----- Properties
    RequestType type;
    Integer requestId;
    Atom    classname;
    Atom    functname;
    Object[] args;

    Object  message;
    // ----- End

    public Request(byte[] bytes) throws Exception {
        this.__pos = 0;
        this.__bytes = bytes;

        if (__bytes[__pos++] == -125) { // 131 ===> Term
            __rawTerm = parse_tag_terms();
        } else if (__bytes[0] == 80) { // 80 ===> Compressed
            // TODO: Un-compressed
        }

        //  FIXME: May crash?
        Tuple request = (Tuple) __rawTerm;
        Atom type = (Atom) request.get(0);
        if (type.value.equals("C")) {
            this.type = RequestType.CALL;
            this.requestId = (Integer) request.get(1);
            this.classname = (Atom) request.get(2);
            this.functname = (Atom) request.get(3);
            this.args      = ((EList) request.get(4)).elements.toArray();

        } else if (type.value.equals("M")) {
            this.type = RequestType.MESSAGE;
            this.message = request.get(1);
        }
    }

    private Object parse_tag_terms() throws Exception {
        Integer tag = parse_unsigned(1).intValue();
        //System.err.printf("Parsing tag %d...\n", tag);

        // SMALL_INTEGER
        if (tag == 97) {
            return parse_unsigned(1).intValue();
        }

        // INTEGER
        if (tag == 98){
            return parse_signed_int32();
        }

        // FLOAT
        if (tag == 99) {
            return parse_float_str(31);
        }

        // NEW_FLOAT
        if (tag == 70) {
            return parse_ieee_float(8);
        }

        // PORT | NEW_PORT
        if (tag == 102 || tag == 89) {
            Atom node = (Atom) parse_tag_terms();
            Long id = parse_unsigned(4);
            Long creation = parse_unsigned(tag == 102 ? 1 : 4);
            return new Port(node, id, creation);
        }

        // PID | NEW_PID
        if (tag == 103 || tag == 88) {
            Atom node = (Atom) parse_tag_terms();
            Long id = parse_unsigned(4);
            Long serial = parse_unsigned(4);
            Long creation = parse_unsigned(tag == 103 ? 1 : 4);
            return new Pid(node, id, serial, creation);
        }

        // SMALL_TUPLE | LARGE_TUPLE
        if (tag == 104 || tag == 105) {
            // FIXME: 4? overflow?
            Integer len = parse_unsigned(tag == 104 ? 1 : 4).intValue();
            Tuple t = new Tuple(len);
            for(Integer i=0; i<len; i++) {
                t.set(i, parse_tag_terms());
            }
            return t;
        }

        // NIL
        if (tag == 106) {
            return new EList();
        }

        // STRING
        if (tag == 107) {
            Integer len = parse_unsigned(2).intValue();
            return parse_string(len);
        }

        // LIST
        if (tag == 108) {
            Long len = parse_unsigned(4);
            EList list = new EList();
            for (Long i=0L; i<len; i++) {
                list.add(parse_tag_terms());
            }
            list.setTail(parse_tag_terms());

            return list;
        }

        // BINARY
        if (tag == 109) {
            Integer len = parse_unsigned(4).intValue();
            return parse_binary(len);
        }

        // ATOM_UTF8 | ATOM
        if (tag == 118 || tag == 100 ) {
            Integer len = parse_unsigned(2).intValue();
            return new Atom(parse_string(len));
        }

        // SMALL_ATOM_UTF8 | SMALL_ATOM
        if (tag == 119 || tag == 115) {
            Integer len = parse_unsigned(1).intValue();
            return new Atom(parse_string(len));
        }

        // REFERENCE
        if (tag == 101) {
            Atom node = (Atom) parse_tag_terms();
            Long id = parse_unsigned(4);
            Long creation = parse_unsigned(1);
            return new Reference(node, id, creation);
        }

        return null;
    }

    // Parse signed integer; bigg-endian; just for 4 bytes
    private Integer parse_signed_int32() {
        Integer c = 0;
        for (int i=1; i<=4; i++,__pos++) {
            c = c | (__bytes[__pos] << ((4-i)*8));
        }
        return c;
    }

    // Parse unsigned integer; bigg-endia; max allowed 4 bytes
    private Long parse_unsigned(Integer len) {
        Long c = 0L;
        for (int i=1; i<=len; i++,__pos++) {
            c = c | (__bytes[__pos] << ((len-i)*8));
        }
        return (c & 0x00000000ffffffff);
    }

    private String parse_float_str(Integer len) {
        String s = new String(__bytes, __pos, len);
        __pos = __pos + len;
        return s;
    }

    private double parse_ieee_float(Integer len) {
        double d = ByteBuffer.wrap(__bytes, __pos, len).getDouble();
        __pos = __pos + len;
        return d;
    }

    private Atom parse_atom_utf8(Integer len) {
        String s = new String(__bytes, __pos, len);
        __pos = __pos + len;
        return new Atom(s);
    }

    private Binary parse_binary(Integer len) {
        Binary b = new Binary(Arrays.copyOfRange(__bytes, __pos, __pos+len));
        __pos = __pos + len;
        return b;
    }

    private String parse_string(Integer len) {
        String s = new String(__bytes, __pos, len);
        __pos = __pos + len;
        return s;
    }

    @Override
    public String toString() {
        if (type == RequestType.MESSAGE) {
            return String.format("Request(type: %s, message: %s)", type, message);
        }
        return String.format(
                "Request(type: %s, requestId: %d, classname: %s, functname: %s, args: %s)",
                 type, requestId, classname, functname, Arrays.toString(args)
                 );
    }
}

class Options extends Object {

    public int packet;

    int compressed;

    int buffer_size;

    String method;

    public Options(final String[] args) {
        for (String s : args) {
            String[] kv = s.split("=");
            if (kv.length < 0) continue;
            switch (kv[0]) {
                case "--packet":
                    packet = Integer.parseInt(kv[1]); break;
                case "--compressed":
                    compressed = Integer.parseInt(kv[1]); break;
                case "--buffer_size":
                    buffer_size = Integer.parseInt(kv[1]); break;
                case "--use_stdio":
                    method = "use_stdio"; break;
                case "--no_use_stdio":
                    method = "no_use_stdio"; break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Option(packet: %d, buffer_size: %d, compressed: %d, method: %s)", packet, buffer_size, compressed, method);
    }
}
