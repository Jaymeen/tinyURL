package org.infinitesolutions.tinyurl.common.util;

public class SnowflakeIdGenerator {
    private static final long EPOCH = 1749354255000L;

    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long MACHINE_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long dataCenterId;
    private final long machineId;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenter ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long timeStamp = currentTimeStamp();

        if(timeStamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if(timeStamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timeStamp = waitUntilNextMillis(timeStamp);
            }
        }
        else {
            sequence = 0L;
        }

        lastTimestamp = timeStamp;

        return ((timeStamp - EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    private long waitUntilNextMillis(long currentMillis) {
        long millis = currentTimeStamp();

        while (millis <= currentMillis) {
            millis = currentTimeStamp();
        }

        return millis;
    }

    private long currentTimeStamp() {
        return System.currentTimeMillis();
    }
}
