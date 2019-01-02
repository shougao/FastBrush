package co.adrianblan.fastbrush.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.adrianblan.fastbrush.MainActivity;
import co.adrianblan.fastbrush.R;
import co.adrianblan.fastbrush.settings.SettingsData;
import co.adrianblan.fastbrush.settings.SettingsManager;

/**
 * Fragment for showing a ink dialog.
 */
public class InkDialogFragment extends DialogFragment {

    @BindView(R.id.seekBarInkOpacity)
    SeekBar seekBarInkOpacity;

    @BindView(R.id.inkBarOpacitySubtitle)
    TextView inkOpacitySubtitle;

    @BindView(R.id.buttonSelectColor)
    AppCompatButton buttonSelectColor;

    @BindView(R.id.colorCircle)
    ImageView colorCircle;

    private SettingsManager settingsManager;
    private SettingsData settingsData;
    private View mainView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        settingsManager = SettingsManager.getInstance(getActivity());
        settingsData = settingsManager.getSettingsData().clone();

        mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_ink, null);
        ButterKnife.bind(this, mainView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Ink Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        settingsManager.saveSettingsData(settingsData);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.setView(mainView);

        final AlertDialog alertDialog = builder.create();

        // Set color to accent color
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.colorAccent));

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(getActivity(), R.color.colorAccent));

                // Set default values, has no effect until dialog is shown
                seekBarInkOpacity.setProgress(settingsData.getColorWrapper().getAlpha());
                colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
            }
        });

        // Set listeners for all elements
        seekBarInkOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float result = progress / 255f;

                if (progress > 0) {
                    settingsData.getColorWrapper().setAlpha(progress);
                    colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
                    inkOpacitySubtitle.setText(String.format("%.2f", result));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ColorPicker cp = new ColorPicker(getActivity(), settingsData.getColorWrapper().getRed(),
                        settingsData.getColorWrapper().getGreen(), settingsData.getColorWrapper().getBlue());

                cp.show();

                // Make button click save color
                Button b = (Button) cp.findViewById(R.id.okColorButton);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsData.getColorWrapper().setColorWithoutAlpha(cp.getColor());
                        colorCircle.setColorFilter(settingsData.getColorWrapper().getColor());
                        cp.dismiss();
                    }
                });

                cp.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        };

        buttonSelectColor.setOnClickListener(onClickListener);
        colorCircle.setOnClickListener(onClickListener);

        return alertDialog;
    }
}