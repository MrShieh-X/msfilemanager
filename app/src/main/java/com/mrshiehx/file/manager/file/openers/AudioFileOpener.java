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
import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.utils.ApplicationUtils;

import java.util.Timer;
import java.util.TimerTask;

public class AudioFileOpener implements FileOpener {
    @Override
    public void open(Context context, AbstractFileItem file) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.file_viewer_audio_player);
        dialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_audio_player, null);
        TextView name = view.findViewById(R.id.audio_name);
        SeekBar progress = view.findViewById(R.id.audio_seekbar);
        TextView remaining = view.findViewById(R.id.audio_remaining);
        TextView total = view.findViewById(R.id.audio_total);
        Button operate = view.findViewById(R.id.audio_operate);
        Button close = view.findViewById(R.id.audio_close);
        name.setText(file.getName());
        dialog.setView(view);
        MediaPlayer mediaPlayer = new MediaPlayer();
        operate.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                operate.setText(R.string.dialog_audio_player_button_play);
            } else {
                mediaPlayer.start();
                operate.setText(R.string.dialog_audio_player_button_stop);
            }
        });

        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            new Thread(mediaPlayer::start).start();
            operate.setText(R.string.dialog_audio_player_button_stop);
            total.setText(millisToTextTime(mediaPlayer.getDuration()));
            progress.setMax(mediaPlayer.getDuration() / 1000);
            AlertDialog alertDialog = dialog.show();

            final boolean[] touching = {false};
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int current = mediaPlayer.getCurrentPosition();
                    String str = millisToTextTime(current);
                    ((Activity) context).runOnUiThread(() -> {
                        if (!touching[0]) {
                            remaining.setText(str);
                            progress.setProgress(current / 1000);
                        }
                    });

                    if (mediaPlayer.isPlaying()) {
                        ((Activity) context).runOnUiThread(() -> operate.setText(R.string.dialog_audio_player_button_stop));
                    } else {
                        ((Activity) context).runOnUiThread(() -> operate.setText(R.string.dialog_audio_player_button_play));
                    }
                }
            }, 0, 200);
            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String str = millisToTextTime(progress * 1000);
                    remaining.setText(str);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    touching[0] = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    touching[0] = false;
                    mediaPlayer.seekTo(seekBar.getProgress() * 1000);
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
                timer.cancel();
                alertDialog.dismiss();
            });
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationUtils.showDialog(context, context.getText(R.string.message_failed_to_play_audio), e.toString(), null, null, null, null, null, null, true);
        }
    }

    private String millisToTextTime(int millis) {
        int seconds = millis / 1000;
        int quotient = seconds / 60;
        int remainder = seconds % 60;

        String sS = String.valueOf(quotient);
        String yS = String.valueOf(remainder);
        if (sS.length() == 1) sS = "0" + sS;
        if (yS.length() == 1) yS = "0" + yS;


        return sS + ":" + yS;
    }
}
