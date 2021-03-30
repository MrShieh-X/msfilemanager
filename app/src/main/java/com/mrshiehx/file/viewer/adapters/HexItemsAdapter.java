package com.mrshiehx.file.viewer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.viewer.beans.HexItem;

import java.util.List;

public class HexItemsAdapter extends ArrayAdapter {
    private final Context context;
    private final List<HexItem> items;

    public HexItemsAdapter(Context context, List<HexItem> items) {
        super(context,R.layout.hex_list_item);
        this.context=context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HexItem item = (HexItem) getItem(position);

        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.hex_list_item, null);
            viewHolder=new ViewHolder();

            viewHolder.first=view.findViewById(R.id.hex_item_1st);
            viewHolder.second=view.findViewById(R.id.hex_item_2nd);
            viewHolder.third=view.findViewById(R.id.hex_item_3rd);
            viewHolder.fourth=view.findViewById(R.id.hex_item_4th);
            viewHolder.fifth=view.findViewById(R.id.hex_item_5th);
            viewHolder.sixth=view.findViewById(R.id.hex_item_6th);
            viewHolder.seventh=view.findViewById(R.id.hex_item_7th);
            viewHolder.eighth=view.findViewById(R.id.hex_item_8th);
            viewHolder.firstR=view.findViewById(R.id.hex_item_right_1st);
            viewHolder.secondR=view.findViewById(R.id.hex_item_right_2nd);
            viewHolder.thirdR=view.findViewById(R.id.hex_item_right_3rd);
            viewHolder.fourthR=view.findViewById(R.id.hex_item_right_4th);
            viewHolder.fifthR=view.findViewById(R.id.hex_item_right_5th);
            viewHolder.sixthR=view.findViewById(R.id.hex_item_right_6th);
            viewHolder.seventhR=view.findViewById(R.id.hex_item_right_7th);
            viewHolder.eighthR=view.findViewById(R.id.hex_item_right_8th);

            view.setTag(viewHolder);
        }else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }



        int t1st=item.getFirst();
        int t2nd=item.getSecond();
        int t3rd=item.getThird();
        int t4th=item.getFourth();
        int t5th=item.getFifth();
        int t6th=item.getSixth();
        int t7th=item.getSeventh();
        int t8th=item.getEighth();

        if(t1st==HexItem.NULL){
            viewHolder.first.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.first.setText(method01(item.getFirst()));
        }

        if(t2nd==HexItem.NULL){
            viewHolder.second.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.second.setText(method01(item.getSecond()));
        }

        if(t3rd==HexItem.NULL){
            viewHolder.third.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.third.setText(method01(item.getThird()));
        }

        if(t4th==HexItem.NULL){
            viewHolder.fourth.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.fourth.setText(method01(item.getFourth()));
        }

        if(t5th==HexItem.NULL){
            viewHolder.fifth.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.fifth.setText(method01(item.getFifth()));
        }

        if(t6th==HexItem.NULL){
            viewHolder.sixth.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.sixth.setText(method01(item.getSixth()));
        }

        if(t7th==HexItem.NULL){
            viewHolder.seventh.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.seventh.setText(method01(item.getSeventh()));
        }

        if(t8th==HexItem.NULL){
            viewHolder.eighth.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.eighth.setText(method01(item.getEighth()));
        }

        viewHolder.firstR.setText(item.getRightFirst());
        viewHolder.secondR.setText(item.getRightSecond());
        viewHolder.thirdR.setText(item.getRightThird());
        viewHolder.fourthR.setText(item.getRightFourth());
        viewHolder.fifthR.setText(item.getRightFifth());
        viewHolder.sixthR.setText(item.getRightSixth());
        viewHolder.seventhR.setText(item.getRightSeventh());
        viewHolder.eighthR.setText(item.getRightEighth());

        return view;
    }

    private CharSequence method01(int i){
        String var=Integer.toHexString(i);
        if(i==100){
            return "";
        }else {
            if (var.length() == 2) {
                return var.toUpperCase();
            } else {
                return ("0" + var).toUpperCase();
            }
        }
    }

    class ViewHolder{
        EditText first;
        EditText second;
        EditText third;
        EditText fourth;
        EditText fifth;
        EditText sixth;
        EditText seventh;
        EditText eighth;
        TextView firstR;
        TextView secondR;
        TextView thirdR;
        TextView fourthR;
        TextView fifthR;
        TextView sixthR;
        TextView seventhR;
        TextView eighthR;
    }
}
