# Wav2c

Wav2c converts RIFF-WAV files to 8Bit 8000Hz mono format and creates a C header file containing raw sounddata
and meta data about the samples.
  
The tool can be used to create audio data which can be played with AVR/Arduino PWM feature.  
  
Put your WAV files into the /sample folder, the newly encoded files will be put into /encoded folder


## Header example

	 
const uint8_t sounddata_sample_count = 8;

enum {<br>
	LENGTH_SAMPLE_0 = 2399,	// 1_BASSDRUM.wav<br>
	LENGTH_SAMPLE_1 = 4282,	// 2_BASSKICK.wav<br>
	...<br>
};<br>
		
const uint16_t sounddata_sample_length_enum[] = { <br>
	LENGTH_SAMPLE_0,<br>
	LENGTH_SAMPLE_1,<br>
	...<br>
};<br>
		
const uint16_t sounddata_length = 19140;
		
const uint16_t sounddata_sample_max_length = 4423;
		
const unsigned char sounddata_data[] PROGMEM = {127,...,1};
 
