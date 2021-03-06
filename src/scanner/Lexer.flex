package scanner;

import java.io.Reader;
import java.lang.Integer;
import java.util.Stack;

%%

%class Lexer
%unicode
%function next
%type Object
%char
%line
%caseless



%{

   /*
   * This has now been extended to cover the main Windows CP1252 characters,
   * at either their correct Unicode codepoints, or in their invalid
   * positions as 8 bit chars inside the iso-8859 control region.
   *
   * ellipses  	85  	0133  	2026  	8230
   * single quote curly starting 	91 	0145 	2018 	8216
   * single quote curly ending 	92 	0146 	2019 	8217
   * double quote curly starting 	93 	0147 	201C 	8220
   * double quote curly ending 	94 	0148 	201D 	8221
   * en dash  	96  	0150  	2013  	8211
   * em dash  	97  	0151  	2014  	8212
   */

  public static final String opendblquote = "``";
  public static final String closedblquote = "''";
  public static final String openparen = "-LRB-";
  public static final String closeparen = "-RRB-";
  public static final String openbrace = "-LCB-";
  public static final String closebrace = "-RCB-";
  public static final String ptbmdash = "--";
  public static final String ptbellipsis = "...";
  /** For tokenizing carriage returns.  (JS) */
  public static final String cr = "*CR*";

  /** This quotes a character with a backslash, but doesn't do it
   *  if the character is already preceded by a backslash.
   */
  private static String delimit (String s, char c) {
    int i = s.indexOf(c);
    while (i != -1) {
      if (i == 0 || s.charAt(i - 1) != '\\') {
        s = s.substring(0, i) + "\\" + s.substring(i);
        i = s.indexOf(c, i + 2);
      } else {
        i = s.indexOf(c, i + 1);
      }
    }
    return s;
  }

  private static String normalizeCp1252(String in) {
    String s1 = in;
    // s1 = s1.replaceAll("[\u0085\u2026]", "..."); // replaced directly
    // s1 = s1.replaceAll("[\u0096\u0097\u2013\u2014]", "--");
    s1 = s1.replaceAll("&apos;", "'");
    // s1 = s1.replaceAll("&quot;", "\""); // replaced directly
    s1 = s1.replaceAll("[\u0091\u2018]", "`");
    s1 = s1.replaceAll("[\u0092\u2019]", "'");
    s1 = s1.replaceAll("[\u0093\u201C]", "``");
    s1 = s1.replaceAll("[\u0094\u201D]", "''");
    s1 = s1.replaceAll("\u00BC", "1\\/4");
    s1 = s1.replaceAll("\u00BD", "1\\/2");
    s1 = s1.replaceAll("\u00BE", "3\\/4");
    s1 = s1.replaceAll("\u00A2", "cents");
    s1 = s1.replaceAll("\u00A3", "#");
    s1 = s1.replaceAll("[\u0080\u20AC]", "$");  // Euro -- no good translation!
    return s1;
  }

  private static String normalizeAmp(final String in) {
    return in.replaceAll("(?i:&amp;)", "&");
  }
	
	//First case of character found in string from multiple characters
	private static int firstCaseIndexOf(String val, String chars) {
		int i = Integer.MAX_VALUE, tmpi;
		//which char comes first, return the index
		for(char c: chars.toCharArray()) {
			tmpi = val.indexOf(c);
			if(tmpi > 0 && tmpi < i) i = tmpi;
		}
		return (i == Integer.MAX_VALUE)? -1: i;
	}

private boolean suppressEscaping = true;
  
private Object getNext() {
    return getNext(yytext(), null);
  }  

private Object getNext(String tokenType) {
    return getNext(yytext(), tokenType);
  }

  private Object getNext(String txt, String tokenType) {
   
	return new Token(txt, tokenType);  
  }
  
  public boolean hasNext() {
  	return zzAtEOF;
  }
  
  //the number of characters from the begining of the file
  public long yychar() {
	  return yychar;
  }
  
  //get the line number of the token
  public int getLine() {
	  return yyline;
  }

  

%}

OPEN = <[A-Za-z!][^>]*>
CLOSE = <\/[A-Za-z!][^>]*>
SPMDASH = &(MD|mdash);|[\u0096\u0097\u2013\u2014]
SPAMP = &amp;
SPPUNC = &(HT|TL|UR|LR|QC|QL|QR|odq|cdq|lt|gt|#[0-9]+);
SPLET = &[aeiouAEIOU](acute|grave|uml);
SPACE = [ \t]+
SPACENL = [ \t\r\n]+
SENTEND = [ \t\n][ \t\n]+|[ \t\n]+([A-Z]|{OPEN}|{CLOSE})
DIGIT = [0-9]
DATE = {DIGIT}{1,2}[\-\/]{DIGIT}{1,2}[\-\/]{DIGIT}{2,4}
NUM = {DIGIT}+|{DIGIT}*([.:,]{DIGIT}+)+
NUMBER = [\-+]?{NUM}|\({NUM}\)
/* Constrain fraction to only match likely fractions */
FRAC = ({DIGIT}{1,4}[- ])?{DIGIT}{1,4}\\?\/{DIGIT}{1,4}
FRAC2 = [\u00BC\u00BD\u00BE]
DOLSIGN = ([A-Z]*\$|#)
DOLSIGN2 = [\u00A2\u00A3\u0080\u20AC]
/* not used DOLLAR	{DOLSIGN}[ \t]*{NUMBER}  */
/* |\( ?{NUMBER} ?\))	 # is for pound signs */
WORD = ([A-Za-z\u00C0-\u00FF]|{SPLET})+
/* The $ was for things like New$ */
/* WAS: only keep hyphens with short one side like co-ed */
/* But treebank just allows hyphenated things as words! */
THING = [A-Za-z0-9]+([_-][A-Za-z0-9]+)*
THINGA = [A-Z]+(([+&]|{SPAMP})[A-Z]+)+
THING3 = [A-Za-z0-9]+(-[A-Za-z]+){0,2}(\\?\/[A-Za-z0-9]+(-[A-Za-z]+){0,2}){1,2}
APOS = ['\u0092\u2019]|&apos;
HTHING = ([A-Za-z0-9][A-Za-z0-9%.,]*(-([A-Za-z0-9]+|{ACRO}\.))+)|[dDOlL]{APOS}{THING}
REDAUX = {APOS}([msdMSD]|re|ve|ll)
/* For things that will have n't on the end. They can't end in 'n' */
SWORD = [A-Za-z]*[A-MO-Za-mo-z]
SREDAUX = n{APOS}t
/* Tokens you want but already okay: C'mon 'n' '[2-9]0s '[eE]m 'till?
   [Yy]'all 'Cause Shi'ite B'Gosh o'clock.  Here now only need apostrophe
   final words. */
APOWORD = {APOS}n{APOS}?|[lLdDjJ]'|Dunkin{APOS}|somethin{APOS}|ol{APOS}|{APOS}em|C{APOS}mon|{APOS}[2-9]0s|{APOS}till?|o{APOS}clock|[A-Za-z][a-z]*[aeiou]{APOS}[aeiou][a-z]*|{APOS}cause
FULLURL = https?:\/\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-]
LIKELYURL = ((www\.([^ \t\n\f\r\"<>|.!?(){},]+\.)+[a-zA-Z]{2,4})|(([^ \t\n\f\r\"`'<>|.!?(){},-_$]+\.)+(com|net|org|edu)))(\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-])?
EMAIL = [a-zA-Z0-9][^ \t\n\f\r\"<>|()]*@([^ \t\n\f\r\"<>|().]+\.)+[a-zA-Z]{2,4}

/* Abbreviations - induced from 1987 WSJ by hand */
ABMONTH = Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec
/* Jun and Jul barely occur, but don't seem dangerous */
ABDAYS = Mon|Tue|Tues|Wed|Thu|Thurs|Fri
/* In caseless, |a\.m|p\.m handled as ACRO, and this is better as can often
   be followed by capitalized. */
/* Sat. and Sun. barely occur and can easily lead to errors, so we omit them */
ABSTATE = Calif|Mass|Conn|Fla|Ill|Mich|Pa|Va|Ariz|Tenn|Mo|Md|Wis|Minn|Ind|Okla|Wash|Kan|Ore|Ga|Colo|Ky|Del|Ala|La|Nev|Neb|Ark|Miss|Vt|Wyo|Tex
ACRO = [A-Za-z](\.[A-Za-z])+|(Canada|Sino|Korean|EU|Japan|non)-U\.S|U\.S\.-(U\.K|U\.S\.S\.R)
ABTITLE = Mr|Mrs|Ms|Miss|Drs?|Profs?|Sens?|Reps?|Lt|Col|Gen|Messrs|Govs?|Adm|Rev|Maj|Sgt|Pvt|Mt|Capt|St|Ave|Pres
ABPTIT = Jr|Bros|Sr
ABCOMP = Inc|Cos?|Corp|Pty|Ltd|Plc|Bancorp|Dept|Mfg|Bhd|Assn
ABNUM = Nos?|Prop|Ph
/* p used to be in ABNUM list, but it can't be any more, since the lexer
   is now caseless.  We don't want to have it recognized for P.  Both
   p. and P. are now under ABBREV4. ABLIST also went away as no-op [a-e] */
/* ABBREV1 abbreviations are normally followed by lower case words.  If
   they're followed by an uppercase one, we assume there is also a
   sentence boundary */
ABBREV3 = {ABMONTH}|{ABDAYS}|{ABSTATE}|{ABCOMP}|{ABNUM}|{ABPTIT}|etc|ft
ABBREV1 = {ABBREV3}\.


/* ABRREV2 abbreviations are normally followed by an upper case word.  We
   assume they aren't used sentence finally */
/* ACRO Is a bad case -- can go either way! */
ABBREV4	= [A-Za-z]|{ABTITLE}|vs|Alex|Cie|a\.k\.a|TREAS|{ACRO}
ABBREV2	= {ABBREV4}\.
/* Cie. is used before French companies */
/* in the WSJ Alex. is generally an abbreviation for Alex. Brown, brokers! */
/* In tables: Mkt. for market Div. for division of company, Chg., Yr.: year */

PHONE = \([0-9]{3}\)\ ?[0-9]{3}[\- ][0-9]{4}
OPBRAC = [<\[]
CLBRAC = [>\]]
HYPHENS = \-+|\u8212+
LDOTS = \.{3,5}|(\.\ ){2,4}\.|[\u0085\u2026]
ATS = @+
UNDS = _+
ASTS = \*+|(\\\*){1,3}
HASHES = #+
FNMARKS = {ATS}|{HASHES}|{UNDS}
INSENTP =[,;:]
QUOTES =`|{APOS}|``|''|[\u2018\u2019\u201C\u201D\u0091\u0092\u0093\u0094]{1,2}
DBLQUOT = \"|&quot;
TBSPEC = -(RRB|LRB|RCB|LCB|RSB|LSB)-|C\.D\.s|D'Amico|M'Bow|pro-|anti-|S&P-500|Jos\.|cont'd\.?|B'Gosh|S&Ls|N'Ko|'twas
TBSPEC2 = {APOS}[0-9][0-9]

%%

//cannot			{ yypushback(3) ; return getNext(); }
{OPEN}			{ return getNext("OPEN-"+yytext().substring(1, firstCaseIndexOf(yytext(), " >")).toUpperCase()); }
{CLOSE}			{ return getNext("CLOSE-"+yytext().substring(2,yylength()-1).toUpperCase()); }
{SPMDASH}		{ if (!suppressEscaping) {
                            return getNext(ptbmdash, "SPMDASH"); }
                          else {
                            return getNext("SPMDASH");
                          }
                        }
{SPAMP}			{ if (!suppressEscaping) {
                            return getNext("&", "SPAMP"); }
                          else {
                            return getNext("SPAMP");
                        }
                   }
{SPPUNC}		{ return getNext("SPPUNC"); }
{WORD}{REDAUX}		{ return getNext("APOSTROPHIZED"); }
{SWORD}{SREDAUX}	{ return getNext("APOSTROPHIZED"); }
{WORD}			{ if (!suppressEscaping) {
                            String word = yytext();
                            return getNext("WORD"); }
                          else {
                            return getNext("WORD");
                          }
                        }
{APOWORD}		{ return getNext("APOWORD"); }
{FULLURL}		{ return getNext("FULLURL"); }
{LIKELYURL}	        { return getNext("LIKELYURL"); }
{EMAIL}			{ return getNext("EMAIL"); }
{REDAUX}/[^A-Za-z]	{ if (!suppressEscaping) {
                            return getNext(normalizeCp1252(yytext()), "REDAUX"); }
                          else {
                            return getNext("REDAUX");
                          }
                        }
{SREDAUX}		{ if (!suppressEscaping) {
                            return getNext(normalizeCp1252(yytext()), "SREDAUX"); }
                          else {
                            return getNext("SREDAUX");
                          }
                        }
{DATE}			{ return getNext("DATE"); }
{NUMBER}		{ return getNext("NUMBER"); }
{FRAC}			{ if (!suppressEscaping) {
                            return getNext(delimit(yytext(), '/'), "FRAC"); }
                          else {
                            return getNext("FRAC");
                          }
                        }
{FRAC2}			{ if (!suppressEscaping) {
                            return getNext(normalizeCp1252(yytext()), "FRAC2");
			  } else {
                            return getNext("FRAC2");
                          }
                        }
{TBSPEC}		{ return getNext("TBSPEC"); }
{THING3}		{ if (!suppressEscaping) {
                            return getNext(delimit(yytext(), '/'), "THING3");
                          } else {
                            return getNext("THING3");
                          }
                        }
{DOLSIGN}		{ return getNext("DOLSIGN"); }
{DOLSIGN2}		{ if (!suppressEscaping) {
                            return getNext(normalizeCp1252(yytext()), "DOLSIGN2"); }
                          else {
                            return getNext("DOLSIGN2");
                          }
                        }
{ABBREV1}/{SENTEND}	{ String s = yytext();
			  yypushback(1);  // return a period for next time
	                  return getNext(s, "ABBREV1"); }           
{ABBREV1}		{ return getNext("ABBREV1"); }
{ABBREV2}		{ return getNext("ABBREV2"); }
{ABBREV4}/{SPACE}	{ return getNext("ABBREV4"); }
{ACRO}/{SPACENL}	{ return getNext("ACRO"); }
{TBSPEC2}/{SPACENL}	{ return getNext("TBSPEC2/SPACENL"); }
{WORD}\./{INSENTP}	{ return getNext("WORD./INSENTP"); }
{PHONE}			{ return getNext("PHONE"); }
{DBLQUOT}/[A-Za-z0-9$]	{ if (!suppressEscaping) {
                            return getNext(opendblquote, "DBLQUOT"); }
                          else {
                            return getNext("DBLQUOT");
                          }
                        }
{DBLQUOT}		{ if (!suppressEscaping) {
                            return getNext(closedblquote, "DBLQUOT"); }
                          else {
                            return getNext("DBLQUOT");
                          }
                        }
\+		{ return getNext("PLUS"); }
%|&		{ return getNext("PIPE"); }
\~|\^		{ return getNext("TILDE"); }
\||\\|0x7f	{ return getNext("DOUBLE-PIPE"); }
{OPBRAC}	{ if (!suppressEscaping) {
                    return getNext(openparen, "OPBRAC"); }
                  else {
                    return getNext("OPBRAC");
                  }
                }
{CLBRAC}	{ if (!suppressEscaping) {
                    return getNext(closeparen, "CLBRAC"); }
                  else {
                    return getNext("CLBRAC");
                  }
                }
\{		{ if (!suppressEscaping) {
                    return getNext(openbrace, "OPBRACE"); }
                  else {
                    return getNext("OPBRACE");
                  }
                }
\}		{ if (!suppressEscaping) {
                    return getNext(closebrace, "CLBRACE"); }
                  else {
                    return getNext("CLBRACE");
                  }
                }
\(		{ if (!suppressEscaping) {
                    return getNext(openparen, "OPPAREN"); }
                  else {
                    return getNext("OPPAREN");
                  }
                }
\)		{ if (!suppressEscaping) {
                    return getNext(closeparen, "CLPAREN"); }
                  else {
                    return getNext("CLPAREN");
                  }
                }
{HYPHENS}	{ if (yylength() >= 3 && yylength() <= 4 && !suppressEscaping) {
	            return getNext(ptbmdash, "HYPHENS");
                  } else {
                    return getNext("HYPHENS");
		  }
		}
{LDOTS}		{ if (!suppressEscaping) {
                    return getNext(ptbellipsis, "LDOTS"); }
                  else {
                    return getNext("LDOTS");
                  }
                }
{FNMARKS}	{ return getNext("FNMARKS"); }
{ASTS}		{ if (!suppressEscaping) {
                    return getNext(delimit(yytext(), '*'), "ASTS"); }
                  else {
                    return getNext("ASTS");
                  }
                }
{INSENTP}	{ return getNext("INSENTP"); }
\.	{ return getNext("PERIOD"); }
\?	{ return getNext("QUESTION"); }
\!	{ return getNext("EXCLAMATION"); }
=		{ return getNext("EQUALS"); }
\/		{ if (!suppressEscaping) {
                    return getNext(delimit(yytext(), '/'), "SLASH"); }
                  else {
                    return getNext("SLASH");
                  }
                }
{HTHING}{REDAUX}			{ return getNext("APOSTROPHIZED"); }
{HTHING}/[^a-zA-Z0-9'.+]    { return getNext("HTTHING"); }
{THING}		{ return getNext("THING"); }
{THINGA}	{ if (!suppressEscaping) {
                    return getNext(normalizeAmp(yytext()), "THINGA"); }
                  else {
                    return getNext("THINGA");
                  }
                }
/* NOTE: An apostrophy with some text after it (that's more than 2 characters is usually a mistake in this implementation therefore
	those characters will not be returned, the only action in this case is to push back just before the quote so the token
	can be read in again.
*/
'[A-Za-z].	{ /* invert quote - using trailing context didn't work.... */
                  String str = yytext();
		  yypushback(2);
                  if (!suppressEscaping) {
                    return getNext("`", "QUOTE"); }
                  else {
                    return getNext(str, "QUOTE");
                  }
                }
{REDAUX}	{ if (!suppressEscaping) {
                    return getNext(normalizeCp1252(yytext()), "REDAUX"); }
                  else {
                    return getNext("REDAUX");
                  }
                }
{QUOTES}	{ if (!suppressEscaping) {
                    return getNext(normalizeCp1252(yytext()), "QUOTES"); }
                  else {
                    return getNext("QUOTES");
                  }
                }
\0|{SPACE}	{ return getNext("SPACE"); }
\n|\r|\r\n	{ return getNext("NEWLINE"); }
&nbsp;		{ return getNext("MLSPACE"); }	
.		{ return getNext("PERIOD"); }
<<EOF>> { return null; }
