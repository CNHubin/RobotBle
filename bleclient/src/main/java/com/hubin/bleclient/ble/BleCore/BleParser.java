package com.hubin.bleclient.ble.BleCore;

import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @项目名： RobotBle
 * @包名： com.hubin.bleclient.ble.BleCore
 * @文件名: BleParser
 * @创建者: 胡英姿
 * @创建时间: 2018/7/25 9:34
 * @描述： 解析蓝牙广播的类
 */
public class BleParser {
    // The following data type values are assigned by Bluetooth SIG.
    // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
    private static final byte DATA_TYPE_FLAGS = 0x01;
    private static final byte DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02; //部分的16位uuid列表
    private static final byte DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03; //完整的16位uuid列表
    private static final byte DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;//部分的32位uuid列表
    private static final byte DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05; //完整的32位uuid列表
    private static final byte DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;//部分的128位uuid列表
    private static final byte DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;//完整的128位uuid列表
    private static final byte DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final byte DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09; //本地设备名称
    private static final byte DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final byte DATA_TYPE_SERVICE_UUIDS_16_BIT_LIST = 0x14; //16位UUID集合
    private static final byte DATA_TYPE_SERVICE_UUIDS_128_BIT_LIST = 0x15;
    private static final byte DATA_TYPE_SERVICE_DATA_16_BIT = 0x16;
    private static final byte DATA_TYPE_SERVICE_DATA_32_BIT = 0x20;
    private static final byte DATA_TYPE_SERVICE_DATA_128_BIT = 0x21;
    private static final byte DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = (byte)0xFF;//制造商特定数据

    private int advertiseFlag = -1;
    private String localName = null;
    private List<ParcelUuid> serviceUuids = new ArrayList<>();
    private short mManufacturerId; //制造商ID
    private byte[] mManufacturerDataBytes; //制造商数据


    public int getAdvertiseFlag() {
        return advertiseFlag;
    }

    public String getLocalName() {
        return localName;
    }

    public List<ParcelUuid> getServiceUuids() {
        return serviceUuids;
    }

    public short getManufacturerId() {
        return mManufacturerId;
    }

    public byte[] getManufacturerDataBytes(short manufacturerId) {
        if (mManufacturerId == manufacturerId) {
            return mManufacturerDataBytes;
        }
        return null;
    }

    public byte[] getManufacturerDataBytes() {
        return mManufacturerDataBytes;
    }

    /**
     * 广播数据分析示例1 携带制造商信息
     * 02011A 0A096875797A2D7669766F 09FFE207A1B2C3D4E5F6 0303A0A
     * 02  长度，接下来的2位为一段数据包
     * 01  flag的标志 0x01
     * 1A  flag数据
     *
     * 0A 长度，接下来的10位为一段数据包
     * 09 本地设备名称的标志 0x09
     * 6875797A2D7669766F 设备名称的数据 转换为String为 huyz-vivo
     *
     * 09 长度，接下来的9位为一段数据包
     * FF 制造商数据的标志 0xff
     * E207 制造商id 转换位short后得到真实id 2018
     * A1B2C3D4E5F6 制造商信息
     *
     * 03 长度，接下来的3位为一段数据包
     * 03 完整的16位UUID的标志
     * A0A0 完整的16位UUID 还原为String为[0000a0a0-0000-1000-8000-00805f9b34fb]
     *
     * @param scanRecord
     * @return
     */
    public BleParser parseData(byte[] scanRecord) {
        //将数据包装到缓冲区并修改为小字节顺序，对于我们常用的CPU架构，如Intel，AMD的CPU使用的都是小字节序，而例如Mac OS以前所使用的Power PC使用的便是大字节序（不过现在Mac OS也使用Intel的CPU了）。
        ByteBuffer buffer = ByteBuffer.wrap(scanRecord).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {//缓冲区中剩余的元素个数大于2
            byte length = buffer.get(); //读取此缓冲区当前位置的字节，然后递增位置。
            if (length == 0){
                break; //数据解析完毕
            }
            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case DATA_TYPE_FLAGS: // advertising 数据的标志
                    advertiseFlag = buffer.get() & 0xff;
                    length--;
                    break;
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL: // Partial list of 16-bit UUIDs
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE: // Complete list of 16-bit UUIDs
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_LIST: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        //buffer.getShort()
                        //在此缓冲区的当前位置读取接下来的两个字节，
                        // 根据当前字节顺序将它们组成一个短值，
                        // 然后将位置递增2。
                        serviceUuids.add(new ParcelUuid(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort()))));

                        length -= 2;
                    }
                    break;
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL: // Partial list of 32 bit service UUIDs
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        //buffer.getInt()
                        //在此缓冲区的当前位置读取接下来的四个字节，
                        // 根据当前字节顺序将它们组成一个int值，
                        //然后将位置增加四。
                        serviceUuids.add(new ParcelUuid(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getInt()))));
                        length -= 4;
                    }
                    break;
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL: // Partial list of 128-bit UUIDs
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE: // Complete list of 128-bit UUIDs
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_LIST: // List of 128-bit Service Solicitation UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        serviceUuids.add(new ParcelUuid(new UUID(msb, lsb)));
                        length -= 16;
                    }
                    break;
                case DATA_TYPE_LOCAL_NAME_SHORT: // 短的本地设备名称
                case DATA_TYPE_LOCAL_NAME_COMPLETE: // 完整的本地设备名称
                    byte hyz[] = new byte[length];
                    buffer.get(hyz, 0, length);
                    length = 0;
                    localName = new String(hyz).trim();
                    break;
                case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA: // Manufacturer Specific Data
                    // 制造商特定数据的前两个字节是制造商ID
                    mManufacturerId = buffer.getShort();
                    length -= 2;
                    //剩下的长度为制造商数据
                    mManufacturerDataBytes = bufferToBytesArr(buffer,length);
//                    manufacturerData.put(manufacturerId,manufacturerDataBytes);
                    length = 0;
                    break;
                default: // skip
                    break;
            }
            if (length > 0) { //不需要的数据
                buffer.position(buffer.position() + length);
            }
        }
        return this;
    }

    /**
     * @param buffer
     * @param length
     * @return
     */
    private byte[] bufferToBytesArr(ByteBuffer buffer, int length) {
        if (buffer == null||length<1) {
            return null;
        }
        byte[] byteArr = new byte[length];
        for (int i = 0; i < length; i++) {
            byteArr[i]=buffer.get();
        }
        return byteArr;
    }

}
