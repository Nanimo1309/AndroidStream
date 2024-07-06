## Linux use

```
pacmd load-module module-null-sink sink_name=android_stream sink_properties=device.description=Android_Stream
nc -l -p 6969 | pacat --rate=44100 --channels=1 --format=s16le --device=
```