package com.hubin.bleclient.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hubin.bleclient.R;
import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.utils.MacUtils;

import java.util.List;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.adapter
 *  @文件名:   MainBleAdapter
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/11 19:35
 *  @描述：    TODO
 */
public class MainBleAdapter extends BaseAdapter{

    private List<BleDevice> mList;;

    private Context mContext;
    public MainBleAdapter(Context context,List<BleDevice> list) {
        mContext = context;
        mList = list;
    }

    public void setList(List<BleDevice> list) {
        if (list != null) {
            mList = list;
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        return mList==null?0:mList.size();
    }


    @Override
    public BleDevice getItem(int position) {
        return mList==null?null:mList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_main_ble_deviced,null);
            holder.name= convertView.findViewById(R.id.item_main_name);
            holder.mac= convertView.findViewById(R.id.item_main_mac);
            holder.rssi= convertView.findViewById(R.id.item_main_rssi);
            holder.time= convertView.findViewById(R.id.item_main_time);
            holder.uuid= convertView.findViewById(R.id.item_main_uuid);
            holder.txPowerLevel= convertView.findViewById(R.id.item_main_TxPowerLevel);
            holder.manufacturerSpecificData= convertView.findViewById(R.id.item_main_ManufacturerSpecificData);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position) != null) {
            String deviceName = getItem(position).getName();

            if (deviceName.equals("huyingzi-robot")||
                deviceName.equals("huyz-vivo")) {
                holder.name.setTextColor(Color.RED);
            } else {
                holder.name.setTextColor(Color.BLUE);
            }

            holder.name.setText("Name:"+deviceName);
            holder.mac.setText("Mac:"+getItem(position).getAddress());
            holder.rssi.setText("Rssi:"+getItem(position).getRssi());

            //纳秒时间差
            long l = getItem(position).getTimestampNanos() - getItem(0).getTimestampNanos();
            holder.time.setText("耗时:"+l/1000000L+"ms"); ///1000000L+"ms"

            holder.uuid.setText("UUID:"+getItem(position).getUuidList().toString());
            holder.txPowerLevel.setText("TxPower:"+getItem(position).getTxPower());

            byte[] manufacturerSpecificData = getItem(position).getManufacturerSpecificData();
            if (manufacturerSpecificData != null) {
                String mac = MacUtils.bytes2HexStringMac(manufacturerSpecificData);
                holder.manufacturerSpecificData.setText("旱地Mac: "+mac);
            } else {
                holder.manufacturerSpecificData.setText("");
            }
        }

        return convertView;
    }

    static class ViewHolder{
        TextView name;
        TextView mac;
        TextView uuid;
        TextView rssi;
        TextView time;
        TextView txPowerLevel;
        TextView manufacturerSpecificData;
    }
}
