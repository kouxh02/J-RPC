package com.tgu.constants;

public final class TransportProtocolConstants {

    private TransportProtocolConstants() {
    }

    public static final int MAGIC = 0x4A525043; // "JRPC"
    public static final byte VERSION = 1;

    public static final int MAGIC_LENGTH = 4;
    public static final int VERSION_LENGTH = 1;
    public static final int FULL_LENGTH_FIELD_LENGTH = 4;
    public static final int TRACE_LENGTH_FIELD_LENGTH = 4;
    public static final int MESSAGE_TYPE_LENGTH = 2;
    public static final int SERIALIZER_TYPE_LENGTH = 2;
    public static final int BODY_LENGTH_FIELD_LENGTH = 4;

    public static final int BASE_HEADER_LENGTH =
            MAGIC_LENGTH +
                    VERSION_LENGTH +
                    FULL_LENGTH_FIELD_LENGTH +
                    TRACE_LENGTH_FIELD_LENGTH +
                    MESSAGE_TYPE_LENGTH +
                    SERIALIZER_TYPE_LENGTH +
                    BODY_LENGTH_FIELD_LENGTH; // 21

    // 预读帧头长度：只包含固定协议头部分，不包含 trace 和 body
    public static final int PRE_FRAME_HEADER_LENGTH =
            MAGIC_LENGTH + VERSION_LENGTH + FULL_LENGTH_FIELD_LENGTH; // 9

    public static final int MAX_TRACE_LENGTH = 8 * 1024; // 8KB
    public static final int MAX_FRAME_LENGTH = 16 * 1024 * 1024; // 16MB
}