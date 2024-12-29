package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import com.nyrds.util.ModdingMode;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.PointF;

import java.util.HashMap;

public class Font extends TextureFilm {
	public static final String SPECIAL_CHAR =
	"àáâäãèéêëìíîïòóôöõùúûüñçÀÁÂÄÃÈÉÊËÌÍÎÏÒÓÔÖÕÙÚÛÜÑÇº¿¡ẞßąęćńóżźłĄĘĆŃÓŻŹŁ";

	public static final String LATIN_UPPER =
	" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String LATIN_FULL = LATIN_UPPER +
	"[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

	public static final String CYRILLIC_UPPER =
	"АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯІЇЄҐЎ";

	public static final String CYRILLIC_LOWER =
	"абвгдеёжзийклмнопрстуфхцчшщъыьэюяіїєґў";

	public static final String ALL_CHARS = LATIN_FULL+SPECIAL_CHAR+CYRILLIC_UPPER+CYRILLIC_LOWER;

	public final SmartTexture texture;

	public float tracking = 0;
	public float baseLine;

	public boolean autoUppercase = false;

	public float lineHeight;

	private boolean endOfRow = false;

	final HashMap<Object, PointF> glyphShift = new HashMap<>();

	protected Font( SmartTexture tx ) {
		super( tx );

		texture = tx;
		texture.filter(Texture.LINEAR,Texture.NEAREST);
		//texture.reload();
	}

	private int findNextEmptyLine(BitmapData bitmap, int startFrom){
		int width  = bitmap.getWidth();
		int height = bitmap.getHeight();

		int nextEmptyLine = startFrom;

		for(; nextEmptyLine < height; ++nextEmptyLine){
			boolean lineEmpty = true;
			for(int i = 0;i<width; ++i){
				lineEmpty = bitmap.isEmptyPixel(i, nextEmptyLine );
				if(!lineEmpty){
					break;
				}
			}
			if(lineEmpty){
				break;
			}
		}
		return nextEmptyLine;
	}

	private boolean isColumnEmpty(BitmapData bitmap, int x, int sy, int ey){
		for(int j = sy; j < ey; ++j){
			if(!bitmap.isEmptyPixel(x,j)){
				//PUtil.slog("Font", "pixel color: " + bitmap.getPixel(x, j) + " color: " + color + " x: " + x + " y: " + j);
				return false;
			}
		}
		//PUtil.slog("Font", "Column " + x + " is empty");
		return true;
	}

	private int findNextCharColumn(BitmapData bitmap, int sx, int sy, int ey){
		int width = bitmap.getWidth();

		int nextEmptyColumn;
		// find first empty column
		for(nextEmptyColumn = sx; nextEmptyColumn < width; ++nextEmptyColumn){
			if(isColumnEmpty(bitmap,nextEmptyColumn, sy, ey)){
				break;
			}
		}

		int nextCharColumn;

		for(nextCharColumn = nextEmptyColumn; nextCharColumn < width; ++nextCharColumn){
			if(!isColumnEmpty(bitmap,nextCharColumn, sy, ey)){
				break;
			}
		}

		if(nextCharColumn == width){
			endOfRow = true;
			return nextEmptyColumn - 1;
		}

		return nextCharColumn-1;
	}


	protected void splitByAlpha(BitmapData bitmap, String chars) {

		autoUppercase = chars.equals( LATIN_UPPER );
		int length    = chars.length();

		int bWidth  = bitmap.getWidth();
		int bHeight = bitmap.getHeight();

		int charsProcessed = 0;
		int lineTop        = 0;
		int lineBottom     = 0;


		while(lineBottom<bHeight){
			while(lineTop==findNextEmptyLine(bitmap, lineTop) && lineTop<bHeight) {
				lineTop++;
			}
			lineBottom = findNextEmptyLine(bitmap, lineTop);
			//GLog.w("Empty line: %d", lineBottom);
			int charColumn = 0;
			int charBorder;

			endOfRow = false;
			while (! endOfRow){
				if(charsProcessed == length) {
					break;
				}

				charBorder = findNextCharColumn(bitmap,charColumn+1,lineTop,lineBottom);

				int glyphBorder = charBorder;
				if(chars.charAt(charsProcessed) != 32) {

					for (;glyphBorder > charColumn + 1; --glyphBorder) {
						if( !isColumnEmpty(bitmap,glyphBorder, lineTop, lineBottom)) {
							break;
						}
					}
					glyphBorder++;
				}

				//GLog.debug("addeded: %d %d %d %d %d",(int)chars.charAt(charsProcessed) ,charColumn, lineTop, glyphBorder, lineBottom);
				add(chars.charAt(charsProcessed),
						new RectF( (float)(charColumn)/bWidth,
								(float)lineTop/bHeight,
								(float)(glyphBorder)/bWidth,
								(float)lineBottom/bHeight ) );
				++charsProcessed;
				charColumn = charBorder;
			}

			lineTop = lineBottom+1;
		}

		//lineHeight = baseLine = height( frames.get( chars.charAt( 0 ) ) );
		lineHeight = baseLine = height( frames.values().iterator().next());
	}

	public static Font colorMarked(String tex_src, String chars ) {
		BitmapData bmp = ModdingMode.getBitmapData(tex_src);
		SmartTexture tex = TextureCache.get( bmp );
		Font font = new Font( tex );
		font.splitByAlpha( bmp, chars );
		//bmp.dispose();
		return font;
	}

	public RectF get( char ch ){
		RectF rec = super.get( autoUppercase ? Character.toUpperCase(ch) : ch );

		// Fix for fonts without accentuation
		if ((rec == null) && (ch > 126)) {
			char tmp;
			String str = (ch + Utils.EMPTY_STRING)
					.replaceAll("[àáâäãą]", "a")
					.replaceAll("[èéêëę]", "e")
					.replaceAll("[ìíîï]", "i")
					.replaceAll("[òóôöõ]", "o")
					.replaceAll("[ùúûü]", "u")
					.replaceAll("[ÀÁÂÄÃĄ]", "A")
					.replaceAll("[ÈÉÊËĘ]", "E")
					.replaceAll("[ÌÍÎÏ]", "I")
					.replaceAll("[ÒÓÔÖÕ]", "O")
					.replaceAll("[ÙÚÛÜ]", "U")
					.replaceAll("[ÙÚÛÜ]", "U")
					.replaceAll("[ñń]", "n")
					.replaceAll("[ÑŃ]", "N")
					.replaceAll("[ŹŻ]", "Z")
					.replaceAll("[źż]", "z")
					.replaceAll("[ÇĆ]", "C")
					.replaceAll("[çć]", "c")
					.replace("Ł", "L")
					.replace("ł", "l")
					.replaceAll("[ŚŞ]", "S")
					.replaceAll("[śş]", "s");

			tmp = str.charAt(0);
			rec = super.get(autoUppercase ? Character.toUpperCase(tmp) : tmp);
		}

		return rec;
	}
}