package de.kai_morich.simple_bluetooth_terminal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private Integer count=0;
    private Byte temp_byte=0x00, end_frame = 0x00;
    private boolean status_message_valid= false;
    private char[] data_array={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private ImageView img_door;
    private ImageView img_phanh;
    private ImageView img_dayantoan;

    private  TextView Text_OutSpeed;
    private  TextView Text_OutEngine;
    private  TextView Text_OutGear;
    private  TextView Text_OutOdometer;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        img_door = view.findViewById(R.id.img_door);
        img_dayantoan = view.findViewById(R.id.img_dayantoan);
        img_phanh = view.findViewById(R.id.img_phanh);

        Text_OutSpeed = view.findViewById(R.id.txt_outspeed);
        Text_OutSpeed.setTextColor(getResources().getColor(R.color.colorRecieveText));
        //Text_OutSpeed.setMovementMethod(ScrollingMovementMethod.getInstance());

        Text_OutEngine = view.findViewById(R.id.txt_outEngineSpeed);
        Text_OutEngine.setTextColor(getResources().getColor(R.color.colorRecieveText));
        //Text_OutEngine.setMovementMethod(ScrollingMovementMethod.getInstance());

        Text_OutOdometer = view.findViewById(R.id.txt_OutOdometer);
        Text_OutOdometer.setTextColor(getResources().getColor(R.color.colorRecieveText));

        Text_OutGear = view.findViewById(R.id.txt_outgear);
        Text_OutGear.setTextColor(getResources().getColor(R.color.colorRecieveText));

        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));

        ImageButton btn_frontleft_up = view.findViewById(R.id.btn_fl_up);
        btn_frontleft_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("a");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("A");
                }
                return false;
            }
        });
        ImageButton btn_frontleft_down = view.findViewById(R.id.btn_fl_down);
        btn_frontleft_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("b");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("B");
                }
                return false;
            }
        });
        ImageButton btn_rearleft_up = view.findViewById(R.id.btn_rl_up);
        btn_rearleft_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("c");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("C");
                }
                return false;
            }
        });
        ImageButton btn_rearleft_down = view.findViewById(R.id.btn_rl_down);
        btn_rearleft_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("d");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("D");
                }
                return false;
            }
        });

        ImageButton btn_frontright_up = view.findViewById(R.id.btn_fr_up);
        btn_frontright_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("e");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("E");
                }
                return false;
            }
        });
        ImageButton btn_frontright_down = view.findViewById(R.id.btn_fr_down);
        btn_frontright_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("f");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("F");
                }
                return false;
            }
        });
        ImageButton btn_rearright_up = view.findViewById(R.id.btn_rr_up);
        btn_rearright_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("g");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("G");
                }
                return false;
            }
        });
        ImageButton btn_rearright_down = view.findViewById(R.id.btn_rr_down);
        btn_rearright_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    send("h");
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    send("H");
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
//            if(hexEnabled) {
//                StringBuilder sb = new StringBuilder();
//                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
//                TextUtil.toHexString(sb, newline.getBytes());
//                msg = sb.toString();
//                data = TextUtil.fromHexString(msg);
//            } else {
                msg = str;
                data = (str + newline).getBytes();
//            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        //check if it is start frame
        Log.d("THINH DEBUG", "receive: ===== "+ new String(data));
        if(data[0] =='!')
        {
           temp_byte = data[0];
           end_frame = 0x00;
           count = 0;
        }

        if(count>=49)
        {
            count=0;
        }
        else
        {
            /*
                Handle Specific issues Size of data change.
             */
            for(int i =0; i< data.length;i++)
            {
                if((char)data[i] == '@')
                {
                    end_frame = '@';
                }
                data_array[count] = (char) data[i];
                count = count + 1;
            }
        }
        if (end_frame == '@' && temp_byte == '!')
        {
            Log.d("THINH DEBUG", "receive: status_message_valid");
            Log.d("THINH DEBUG", new String(data_array));
            //check frame lenght
            /*
                Format: StartByte-Data(Char)-StopByte
             */
            int temp_num_byte = 1;
            status_message_valid = true;
            if (data_array[temp_num_byte] == '1') {
                img_door.setVisibility(View.VISIBLE);
            } else {
                img_door.setVisibility(View.INVISIBLE);
            }
            temp_num_byte++;
            if (data_array[temp_num_byte] == '1') {
                img_phanh.setVisibility(View.VISIBLE);
            } else {
                img_phanh.setVisibility(View.INVISIBLE);
            }
            temp_num_byte++;
            if (data_array[temp_num_byte] == '1') {
                img_dayantoan.setVisibility(View.VISIBLE);
            } else {
                img_dayantoan.setVisibility(View.INVISIBLE);
            }
            temp_num_byte++;
            char[] speed_array = {0, 0, 0};
            speed_array[0] = data_array[temp_num_byte];
            temp_num_byte++;
            speed_array[1] = data_array[temp_num_byte];
            temp_num_byte++;
            speed_array[2] = data_array[temp_num_byte];
            temp_num_byte++;

            Text_OutSpeed.setText(new String(speed_array)+ " km/h");

            char[] eng_speed_array = {0, 0, 0, 0, 0};
            eng_speed_array[0] = data_array[temp_num_byte];temp_num_byte++;
            eng_speed_array[1] = data_array[temp_num_byte];temp_num_byte++;
            eng_speed_array[2] = data_array[temp_num_byte];temp_num_byte++;
            eng_speed_array[3] = data_array[temp_num_byte];temp_num_byte++;
            eng_speed_array[4] = data_array[temp_num_byte];temp_num_byte++;
            Text_OutEngine.setText(new String(eng_speed_array)+ " rpm");

            char tempchar = data_array[temp_num_byte];
            if (tempchar == '0') {
                Text_OutGear.setText("P");
            }
            else if (tempchar == '1')
            {
                Text_OutGear.setText("N");
            }
            else if (tempchar == '2')
            {
                Text_OutGear.setText("D");
            }
            else if (tempchar == '3')
            {
                Text_OutGear.setText("R");
            }
            else if (tempchar == '4')
            {
                Text_OutGear.setText("S");
            }
            else if (tempchar == '5')
            {
                Text_OutGear.setText("L");
            }
            else
            {
                Text_OutGear.setText("Invalid");
            }
            temp_num_byte++;

            char[] Odometer_array = {0, 0, 0, 0, 0, 0};
            Odometer_array[0] = data_array[temp_num_byte];temp_num_byte++;
            Odometer_array[1] = data_array[temp_num_byte];temp_num_byte++;
            Odometer_array[2] = data_array[temp_num_byte];temp_num_byte++;
            Odometer_array[3] = data_array[temp_num_byte];temp_num_byte++;
            Odometer_array[4] = data_array[temp_num_byte];temp_num_byte++;
            Odometer_array[5] = data_array[temp_num_byte];temp_num_byte++;
            Text_OutOdometer.setText(new String(Odometer_array)+ " km");

            ///////Clear all value//////
            temp_byte = 0x00;
            end_frame = 0x00;
            count = 0;
            status_message_valid = false;
        }

//        /////////////////////////////////////////////////
//        if (hexEnabled) {
//            receiveText.append(TextUtil.toHexString(data) + '\n');
//        } else {
//            String msg = new String(data);
//            if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
//                // don't show CR as ^M if directly before LF
//                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
//                // special handling if CR and LF come in separate fragments
//                if (pendingNewline && msg.charAt(0) == '\n') {
//                    Editable edt = receiveText.getEditableText();
//                    if (edt != null && edt.length() > 1)
//                        edt.replace(edt.length() - 2, edt.length(), "");
//                }
//                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
//            }
//            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
//        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

}
