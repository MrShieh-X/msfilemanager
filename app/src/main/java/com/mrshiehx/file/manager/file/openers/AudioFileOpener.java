package com.mrshiehx.file.manager.file.openers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.utils.ApplicationUtils;

import java.io.File;
import java.io.IOException;

public class AudioFileOpener implements FileOpener{
    @Override
    public void open(Context context, File file) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setTitle(R.string.file_viewer_audio_player);
        dialog.setCancelable(false);
        View view=LayoutInflater.from(context).inflate(R.layout.dialog_audio_player,null);
        TextView name=view.findViewById(R.id.audio_name);
        SeekBar progress=view.findViewById(R.id.audio_seekbar);
        TextView remaining=view.findViewById(R.id.audio_remaining);
        TextView total=view.findViewById(R.id.audio_total);
        Button operate=view.findViewById(R.id.audio_operate);
        Button close=view.findViewById(R.id.audio_close);
        name.setText(file.getName());
        dialog.setView(view);
        MediaPlayer mediaPlayer = new MediaPlayer();
        operate.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                operate.setText(R.string.dialog_audio_player_button_play);
            }else{
                mediaPlayer.start();
                operate.setText(R.string.dialog_audio_player_button_stop);
            }
        });
        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.start();
            operate.setText(R.string.dialog_audio_player_button_stop);
            total.setText(method01(mediaPlayer.getDuration()));
            progress.setMax(mediaPlayer.getDuration()/1000);
            AlertDialog alertDialog=dialog.show();
            Thread thread=new Thread(()->{
                while (true){
                    String str=method01(mediaPlayer.getCurrentPosition());
                    ((Activity)context).runOnUiThread(()->{
                        remaining.setText(str);
                        progress.setProgress(mediaPlayer.getCurrentPosition()/1000);
                    });
                    if(mediaPlayer.isPlaying()){
                        ((Activity)context).runOnUiThread(()->operate.setText(R.string.dialog_audio_player_button_stop));
                    }else{
                        ((Activity)context).runOnUiThread(()->operate.setText(R.string.dialog_audio_player_button_play));
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mediaPlayer.seekTo(seekBar.getProgress()*1000);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            /*dialog.setOnCancelListener(dialog1 ->{
                mediaPlayer.stop();
                thread.interrupt();
                //thread2.interrupt();
                alertDialog.dismiss();
            });*/
            close.setOnClickListener(var -> {
                mediaPlayer.stop();
                thread.interrupt();
                //thread2.interrupt();
                alertDialog.dismiss();
            });
        }catch (IOException e){
            e.printStackTrace();
            ApplicationUtils.showDialog(context,context.getText(R.string.message_failed_to_play_audio),e.toString(),null,null,null,null,null,null,true);
        }
    }

    private String method01(int millis){
        int seconds=millis/1000;
        int shang=seconds/60;
        int yushu=seconds%60;

        String sS=String.valueOf(shang);
        String yS=String.valueOf(yushu);
        if(sS.length()==1)sS="0"+sS;
        if(yS.length()==1)yS="0"+yS;


        return sS+":"+yS;
    }
}
