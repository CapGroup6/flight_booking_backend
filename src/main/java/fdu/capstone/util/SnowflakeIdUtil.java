package fdu.capstone.util;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
public class SnowflakeIdUtil {

    /** start time (2015-01-01) */
    private final long twepoch = 1489111610226L;

    /** machine id bits */
    private final long workerIdBits = 5L;

    /** data center id bits */
    private final long dataCenterIdBits = 5L;

    /** max machine id */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** max data center id*/
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);

    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;

    private final long dataCenterIdShift = sequenceBits + workerIdBits;

    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;

    private long dataCenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    // ==============================Constructors=====================================

    public SnowflakeIdUtil(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenterId can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    // ==============================Methods==========================================

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) //
                | (dataCenterId << dataCenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }


    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }


    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
