package net.mikaboshi.velocity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Apache Jakarta Velocity に関するユーティリティクラス。
 * 
 * @author Takuma Umezawa
 *
 */
public final class VelocityUtils {
	
	private static Log logger = LogFactory.getLog(VelocityUtils.class);

	private VelocityUtils() {}
	
	/**
	 * Velocity テンプレートファイルから実ファイルを生成する。
	 * 
	 * @param templateFile Velocity テンプレートファイル
	 * @param outputFile　生成される実ファイル
	 * @param templateEncoding テンプレートファイルのエンコーディング
	 * @param outputEncoding 生成される実ファイルのエンコーディング
	 * @param context
	 * 			テンプレートに埋め込むパラメータがセットされた VelocityContext オブジェクト。
	 * 			パラメータが無い場合は、null 可。
	 * 
	 * @throws UnsupportedEncodingException 指定したエンコーディングが不正な場合
	 * @throws IOException 生成中にエラーが発生した場合
	 */
	public static void weave(
			File templateFile,
			File outputFile,
			String templateEncoding,
			String outputEncoding,
			VelocityContext context)
			throws IOException {
		
		Writer writer = null;
		OutputStream output = null;

		try {
			Template template = Velocity.getTemplate(
					getRelativePath(templateFile), templateEncoding);
			
			output = FileUtils.openOutputStream(outputFile);
			
			writer = new BufferedWriter(
					new OutputStreamWriter(output, outputEncoding));
			
			if (context == null) {
				context = new VelocityContext();
			}
			
			template.merge(context, writer);
			
			writer.flush();
			
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(writer);
		}
	}
	
	/**
	 * カレントディレクトリからの相対パスを取得する。
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static String getRelativePath(File file) throws IOException {
		String curPath = new File(".").getCanonicalPath();
		String targetPath = file.getCanonicalPath();
		
		return targetPath.substring(curPath.length() + 1);
	}
}
