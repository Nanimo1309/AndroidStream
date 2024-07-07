## Linux use

```
load-module module-null-sink sink_name=mic_sink sink_properties=device.description=Mic_Sink
load-module module-simple-protocol-tcp port=6969 rate=32000 format=s16le channels=1 sink=mic_sink
```
