# Application for stream android microphone input to your linux pc

Remember to change ip and port for your own

### Pulseaudio config

Run this in bash with pactl or put it in /ect/pulse/default.pa.d/myConfig.pa
Also remember to change port ;)

```
load-module module-null-sink sink_name=mic_sink sink_properties=device.description=Mic_Sink
load-module module-simple-protocol-tcp port=6969 rate=32000 format=s16le channels=1 sink=mic_sink
```
