package com.github.mygreen.cellformatter.term;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import com.github.mygreen.cellformatter.lang.MSLocale;
import com.github.mygreen.cellformatter.lang.Utils;
import com.github.mygreen.cellformatter.number.FormattedNumber;
import com.github.mygreen.cellformatter.number.NumberPartType;
import com.github.mygreen.cellformatter.tokenizer.Token;


/**
 * 数値の書式の項
 * 
 * @version 0.8
 * @author T.TSUCHIE
 *
 */
public abstract class NumberTerm implements Term<FormattedNumber> {
    
    public static GeneralTerm general() {
        return new GeneralTerm();
    }
    
    public static ZeroTerm zero() {
        return new ZeroTerm();
    }
    
    public static SharpTerm sharp() {
        return new SharpTerm();
    }
    
    public static QuestionTerm question() {
        return new QuestionTerm();
    }
    
    public static ExponentTerm exponnet(final Token token) {
        return new ExponentTerm(token);
    }
    
    public static SeparatorTerm separator(final Token.Symbol token) {
        return new SeparatorTerm(token);
    }
    
    public static SymbolTerm symbol(final Token.Symbol token) {
        return new SymbolTerm(token);
    }
    
    public static DigitsTerm digits(final Token.Digits token) {
        return new DigitsTerm(token);
    }
    
    /**
     * フォーマットの書式"General"を表現する項
     *
     */
    public static class GeneralTerm extends NumberTerm {
        
        @Override
        public String format(final FormattedNumber number, final MSLocale formatLocale, final Locale runtimeLocale) {
            
            final double unsingedValue = Math.abs(number.getValue());
            
            // 指数表記の場合
            if(isNumberAsExponent(unsingedValue)) {
                final DecimalFormat format = new DecimalFormat("0.#####E0");
                format.setRoundingMode(RoundingMode.HALF_UP);
                
                String str = format.format(unsingedValue);
                if(unsingedValue >= 1) {
                    // 指数に符号を付ける
                    str = str.replace("E", "E+");
                }
                
                return str;
                
            }
            
            final BigDecimal num = new BigDecimal(unsingedValue);
            final String strNum = num.toPlainString();
            
            /*
             * 小数部がない場合
             * ・小数点で判断
             */
            if(!strNum.contains(".")) {
                return strNum;
            }
            
            /*
             * 小数部がある場合、整数部の桁数によって精度を変える
             * ・整数部が10桁以上ある場合は、小数部は省略される。
             * ・整数部が10桁未満の場合、有効桁数が10桁になるように少数の精度が増える。
             */
            final String strIntPart = strNum.substring(0, strNum.indexOf("."));
            final int intLength = strIntPart.length();
            
            final String pattern;
            if(intLength < 10) {
                StringBuilder f = new StringBuilder();
                f.append("0.");
                for(int i=0; i < 10-intLength; i++) {
                    f.append("#");
                }
                
                pattern = f.toString();
                
            } else {
                pattern = "0";
            }
            
            final DecimalFormat format = new DecimalFormat(pattern);
            format.setRoundingMode(RoundingMode.HALF_UP);
            return format.format(unsingedValue);
            
        
        }
        
        /**
         * 指数表示すべき数値を判定する
         * @param unsingedValue 符号なしの数値
         * @return true 指数表示する。
         */
        private boolean isNumberAsExponent(final double unsingedValue) {
            
            if(unsingedValue == 0.0d) {
                return false;
                
            } else if(unsingedValue >= 100000000000.0d) {
                return true;
                
            } else if(unsingedValue <= 0.0000000001d) {
                return true;
                
            } else {
                return false;
            }
            
        }
    }
    
    /**
     * 数値のフォーマット部分の項を表す抽象クラス。
     *
     */
    public static abstract class FormattedTerm extends NumberTerm {
        
        /** 桁のインデックス */
        protected int index;
        
        /** 書式の部分 */
        protected NumberPartType partType;
        
        /** 書式の部分の最後かどうか */
        protected boolean lastPart;
        
        /** 桁の区切り文字を出力するかどうか */
        protected boolean outSepearator;
        
        public FormattedTerm index(final int index) {
            this.index = index;
            return this;
        }
        
        public FormattedTerm partType(final NumberPartType partType) {
            this.partType = partType;
            return this;
        }
        
        public FormattedTerm lastPart(final boolean lastPart) {
            this.lastPart = lastPart;
            return this;
        }
        
        /**
         * 数値の部分に対する桁の値を取得する。
         * @param number
         * @return
         */
        protected String getNumber(final FormattedNumber number) {
            
            switch(partType) {
                case Integer:
                    if(isLastPart()) {
                        return number.asDecimal().getIntegerPartAfter(getIndex());
                    } else {
                        return number.asDecimal().getIntegerPart(getIndex());
                    }
                    
                case Decimal:
                    return number.asDecimal().getDecimalPart(getIndex());
                    
                case Exponent:
                    if(isLastPart()) {
                        return number.asExponent().getExponentPartAfter(getIndex());
                    } else {
                        return number.asExponent().getExponentPart(getIndex());
                    }
                case Denominator:
                    if(isLastPart()) {
                        return number.asFraction().getDenominatorPartAfter(getIndex());
                    } else {
                        return number.asFraction().getDenominatorPart(getIndex());
                    }
                    
                case Numerator:
                    
                    if(isLastPart()) {
                        return number.asFraction().getNumeratorPartAfter(getIndex());
                    } else {
                        return number.asFraction().getNumeratorPart(getIndex());
                    }
                    
                case WholeNumber:
                    if(isLastPart()) {
                        return number.asFraction().getWholeNumberPartAfter(getIndex());
                    } else {
                        return number.asFraction().getWholeNumberPart(getIndex());
                    }
                    
                default:
                    return "";
            }
            
            
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public NumberPartType getPartType() {
            return partType;
        }
        
        public void setPart(NumberPartType partType) {
            this.partType = partType;
        }
        
        public boolean isLastPart() {
            return lastPart;
        }
        
        public void setLastPart(boolean lastPart) {
            this.lastPart = lastPart;
        }
        
        public boolean isOutSepearator() {
            return outSepearator;
        }
        
        public void setOutSepearator(boolean outSepearator) {
            this.outSepearator = outSepearator;
        }
        
    }
    
    /**
     * フォーマットの書式"0"の記号。
     * ・出力する項がない場合は、0を出力する。
     */
    public static class ZeroTerm extends FormattedTerm {
        
        private static final String ZERO = "0";
        
        @Override
        public String format(final FormattedNumber number, final MSLocale formatLocale, final Locale runtimeLocale) {
            
            String num = getNumber(number);
            if(num.isEmpty()) {
                return ZERO;
            }
            
            return num;
        }
        
    }
    
    /**
     * フォーマットの書式"#"の記号。
     * ・出力する項がない場合は、何も出力しない。
     *
     */
    public static class SharpTerm extends FormattedTerm {
        
        @Override
        public String format(final FormattedNumber number, final MSLocale formatLocale, final Locale runtimeLocale) {
            String num = getNumber(number);
            return num;
        }
        
    }
    
    /**
     * フォーマットの書式"?"の記号。
     * ・出力する項がない場合は、半角スペースを出力する。
     *
     */
    public static class QuestionTerm extends FormattedTerm {
        
        private static final String SPACE = " ";
        
        @Override
        public String format(final FormattedNumber number, final MSLocale formatLocale, final Locale runtimeLocale) {
            String num = getNumber(number);
            if(num.isEmpty()) {
                return SPACE;
            }
            
            return num;
        }
        
    }
    
    /**
     * フォーマットの書式の指数"E"を表現する項。
     * ・指数部の符号も出力する。
     *
     */
    public static class ExponentTerm extends NumberTerm {
        
        /**
         * 符号
         * ・ただし、符号がない場合がある。
         * ・出力するときには、設定された符号は無視する。
         */
        private final Token token;
        
        /**
         * 指数の記号。
         * ・パターンによって、大文字、小文字がある。
         */
        private final String exponentSymbol;
        
        public ExponentTerm(final Token token) {
            this.token = token;
            
            final String vale = token.getValue();
            if(vale.startsWith("E")) {
                this.exponentSymbol = "E";
            } else {
                this.exponentSymbol = "e";
            }
        }
        
        @Override
        public String format(final FormattedNumber number, final MSLocale formatLocale, final Locale runtimeLocale) {
            
            if(number.asExponent().isExponentPositive()) {
                if(Utils.startsWithIgnoreCase(getToken().getValue(), "E-")) {
                    // 指数がマイナスの場合は、正の時に符号は付与しない。
                    return exponentSymbol;
                } else {
                    return exponentSymbol + "+";
                }
            } else {
                return exponentSymbol + "-";
            }
        }
        
        public Token getToken() {
            return token;
        }
        
        public String getExponentSymbol() {
            return exponentSymbol;
        }
        
    }
    
    /**
     * 桁区切り文字の処理
     * ・区切り文字の挿入は、数値の出力時の行う。
     */
    public static class SeparatorTerm extends NumberTerm {
        
        private final Token.Symbol token;
        
        public SeparatorTerm(final Token.Symbol token) {
            this.token = token;
        }
        
        @Override
        public String format(final FormattedNumber value, final MSLocale formatLocale, final Locale runtimeLocale) {
            return "";
        }
        
        public Token.Symbol getToken() {
            return token;
        }
        
    }
    
    /**
     * 記号の処理
     *
     * @author T.TSUCHIE
     *
     */
    public static class SymbolTerm extends NumberTerm {
        
        private final Token.Symbol token;
        
        public SymbolTerm(final Token.Symbol token) {
            this.token = token;
        }
        
        @Override
        public String format(final FormattedNumber value, final MSLocale formatLocale, final Locale runtimeLocale) {
            return token.getValue();
        }
        
        public Token.Symbol getToken() {
            return token;
        }
        
    }
    
    public static class DigitsTerm extends NumberTerm {
        
        private final Token.Digits token;
        
        public DigitsTerm(Token.Digits token) {
            this.token = token;
        }
        
        @Override
        public String format(final FormattedNumber value, final MSLocale formatLocale, final Locale runtimeLocale) {
            return token.getValue();
        }
        
        public Token.Digits getToken() {
            return token;
        }
        
    }
    
}
