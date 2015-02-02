# Wav2c

Wav2c converts RIFF-WAV files to 8Bit 8000Hz mono format and creates a C header file containing raw sounddata
and meta data about the samples.
  
The tool can be used to create audio data which can be played with AVR/Arduino PWM feature.  
  
Put your WAV files into the /sample folder, the newly encoded files will be put into /encoded folder


HEADER EXAMPLE
==============
	 
const uint8_t sounddata_sample_count = 8;
	
enum {
	LENGTH_SAMPLE_0 = 2399,	// 1_BASSDRUM.wav
	LENGTH_SAMPLE_1 = 4282,	// 2_BASSKICK.wav
	...
};
		
const uint16_t sounddata_sample_length_enum[] = { 
	LENGTH_SAMPLE_0,
	LENGTH_SAMPLE_1,
	...
};
		
const uint16_t sounddata_length = 19140;
		
const uint16_t sounddata_sample_max_length = 4423;
		
const unsigned char sounddata_data[] PROGMEM = {127,...,1};
 
