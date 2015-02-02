/**
 * 
 * Wav2c converts WAV Files to 8Bit 8000Hz mono format and creates a C header file containing raw sounddata
 * and meta data about the samples.
 * 
 * The tool can be used to create audio data which can be played with AVR/Arduino PWM feature.  
 * 
 * Put your WAV files into the /sample folder, the newly encoded files will be put into /encoded folder
 * 
 * 
 */
package wav2c;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Wav2c {
	/*	 
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
		  
	 */
	private final static String CR 						= "\n";
	private final static String LE 						= ";"+CR+CR;
	private final static String SAMPLES 				= "const uint8_t sounddata_sample_count = ";
	
	
	private final static String ENUM_PREFIX 			= "enum {"+CR;
	private final static String ENUM_COMMENT 			= "\t//";
	private final static String ENUM_SUFFIX 			= "};"+CR+CR;
	private final static String ENUM_SAMPLE_PREFIX 		= "\tLENGTH_SAMPLE_";
	
	private final static String LENGTH_ARR_PREFIX 		= "const uint16_t sounddata_sample_length_enum[] = { "+CR;
	private final static String LENGTH_ARR_SUFFIX 		= "};"+CR+CR;
	
	
	private final static String TOTAL_SAMPLE_LENGTH 	= "const uint16_t sounddata_length = ";
	private final static String MAX_SAMPLE_LENGTH 		= "const uint16_t sounddata_sample_max_length = ";
	private final static String PROGMEM_PREFIX 			= "const unsigned char sounddata_data[] PROGMEM = {";
	private final static String PROGMEM_SUFFIX 			= "};"+CR ;
	private final static int 	BITRATE 				= 8;
	private final static int 	CHANNELS 				= 1;
	private final static float 	FREQUENCY 				= 8000.0F;
	private final static String SAMPLE_DIR 				= "samples";
	private final static String ENCODED_DIR 			= "encoded";
	private final static String HEADER_NAME 			= "sounddata.h";
	
	private final static AudioFormat.Encoding 	TARGET_ENCODING 	=  AudioFormat.Encoding.PCM_UNSIGNED;
	private final static AudioFormat 			TARGET_AUDIO_FORMAT = new AudioFormat(TARGET_ENCODING, FREQUENCY, BITRATE, CHANNELS, 1,1, false);
	private final static File 					HEADER_FILE 		= new File(HEADER_NAME);
		
	private String 	strSampleCount 			= SAMPLES;
	private String 	strSampleLengthEnum 	= ENUM_PREFIX;
	private String 	strSampleLengthArr 		= LENGTH_ARR_PREFIX;
	private String 	strSampleTotalLength 	= TOTAL_SAMPLE_LENGTH;
	private String 	strSampleMaxLength 		= MAX_SAMPLE_LENGTH;
	private String 	strProgmem 				= PROGMEM_PREFIX;
	private int 	maxSampleLength			= 0;
	private int 	totalSampleLength 		= 0;
	private int 	fileNum 				= 0;
	private int 	cropSize 				= -1;
	private boolean writeEncWav 			= true;
	
	public static void main(final String[] args) 
	{
		Wav2c mw2c= new Wav2c();
		mw2c.init(args);
	}
	
	public void init (final String[] args)
	{
		if (args.length > 1)
		{
			printUsage();
			System.exit(0);
		}
		
		if (args.length == 1)
		{
			cropSize = Integer.parseInt(args[0]);
		}
		
		final File 	dir = new File(SAMPLE_DIR);
		final File[] 	files = dir.listFiles();
		
		Arrays.sort(files);
		
		strSampleCount += files.length + LE;
		
		if (cropSize > 0)
		{
			// TODO make a first pass
		}
		
		for(final File f : files) 
		{
		   this.decode(fileNum, f);
		   fileNum++;
		}
		
		// close all strings
		strSampleLengthEnum 	+= ENUM_SUFFIX;
		strSampleLengthArr 		+= LENGTH_ARR_SUFFIX;
		strSampleTotalLength 	+= totalSampleLength + LE;
		strSampleMaxLength 		+= maxSampleLength + LE;
		strProgmem 				+= PROGMEM_SUFFIX;
		
		// save header
		this.createHeader();
	}

	public void decode(final int fileNum, final File encodedFile)
	{

		AudioInputStream ais = null;
		
		try
		{
			ais = AudioSystem.getAudioInputStream(encodedFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (ais == null)
		{
			out("cannot open input file");
			System.exit(1);
		}
	
		out("AudioDecoder: ais: " + ais);
		out("AudioDecoder: ais AudioFormat: " + ais.getFormat());
		out("AudioDecoder: ais length (frames): " + ais.getFrameLength());
		
		AudioInputStream encodedAudioInputStream = null;
		
		if (TARGET_AUDIO_FORMAT != null)
		{
			encodedAudioInputStream = AudioSystem.getAudioInputStream(TARGET_AUDIO_FORMAT, ais);
			
			out("AudioDecoder: eis: " + encodedAudioInputStream); 
			out("AudioDecoder: eis AudioFormat: " + encodedAudioInputStream.getFormat()); 
			
			final byte[] arr = toByte(encodedAudioInputStream);
			final int length = arr.length;
			
			String dataArray = "";
			int counter = 0;
						
			for (byte b:arr)
			{
				b = (byte)Math.round((float)b * (1f/Math.sqrt(2f)));
				final int x = b & 0xff;
				
				if (counter != 0 || fileNum > 0)
				{
					dataArray +=",";
					if (counter % 16 == 0)
						dataArray += CR;
				}				
				dataArray += x;				
				counter++;
			}
			
			strSampleLengthEnum += ENUM_SAMPLE_PREFIX + fileNum+" = "+length+","+ENUM_COMMENT+" "+encodedFile.getName()+CR;
			strSampleLengthArr 	+= ENUM_SAMPLE_PREFIX + fileNum+","+CR;
			totalSampleLength 	+= length;
			
			if (length > maxSampleLength)
				maxSampleLength = length;
			
			strProgmem += dataArray;
			
			createHeader();
			
			// create wav files to check quality
			if (writeEncWav)
			{
				try
				{
					final AudioInputStream encodedAudioInputStreamSave = AudioSystem.getAudioInputStream(TARGET_AUDIO_FORMAT, AudioSystem.getAudioInputStream(encodedFile));
					final File pcmFile = new File(ENCODED_DIR+"/"+encodedFile.getName().substring(0, encodedFile.getName().lastIndexOf("."))+"_"+BITRATE+"_"+CHANNELS+"_"+(int)FREQUENCY+".wav");
					final int nWrittenFrames = AudioSystem.write(encodedAudioInputStreamSave, AudioFileFormat.Type.WAVE, pcmFile);
			
					out("File "+pcmFile.getName()+" writtenFrames:"+nWrittenFrames); 
				}
				catch (IOException e)
				{
					e.printStackTrace();
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				}				
			}
		}		
	}
	
	public static byte[] toByte(final InputStream is)
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		final byte[] data = new byte[1024 * 1024];

		try 
		{
			while ((nRead = is.read(data, 0, data.length)) != -1) 
			{
			  buffer.write(data, 0, nRead);
			  out("toByte length: "+nRead); 
			}
			buffer.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	
	public  void createHeader()
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(HEADER_FILE));
			writer.write(strSampleCount);
			writer.write(clean(strSampleLengthEnum));
			writer.write(clean(strSampleLengthArr));
			writer.write(strSampleTotalLength);
			writer.write(strSampleMaxLength);
			writer.write(strProgmem);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				writer.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}
    }
	
	private String clean(final String s)
	{
		return s.substring(0,s.lastIndexOf(","))+s.substring(s.lastIndexOf(",")+1,s.length());
	}
	
	private static void printUsage()
	{
		out("Usage: java Wav2c");
	}
	
	private static void out(final String strMessage)
	{
		System.out.println(strMessage);
	}
	
}
