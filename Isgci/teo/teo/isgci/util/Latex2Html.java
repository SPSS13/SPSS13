/*
 * Convert latex to html.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/Latex2Html.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Latex2Html extends Latex {

    /**
     * Path to the location of the image files. Should end in /.
     */
    protected String imgpath;

    /**
     * Create a new latex->html converter with images at the given location.
     */
    public Latex2Html(String imgpath) {
        super();
        this.imgpath = "file:data/images/";
    }

    /**
     * Return s as an html string.
     */
    public String htmlTruncated(String s) {

        HtmlState state = new HtmlState(s);
        drawLatexPart(state, true);

        String htmlName = state.target.toString();

        try {
            htmlName = "<div><table><tr><td style=\"margin-top:2;\">"
                    + htmlName + "</td></tr></table></div>";

            String panelTextBegin = "<html><body style=\"max-height:4;\">";
            String panelTextEnd = "</body></html>";
            htmlName = panelTextBegin + truncateHTML(htmlName, 40, "...")
                    + panelTextEnd;

        } catch (Exception e) {
        }

        return htmlName;

    }

    public String html(String s) {
        HtmlState state = new HtmlState(s);
        drawLatexPart(state, true);
        return "<div><table><tr><td style=\"margin-top:2;\">"
                + state.target.toString() + "</td></tr></table></div>";
    }

    protected State startSuper(State s) {
        ((HtmlState)s).target.append("<sup>");
        return super.startSuper(s);
    }

    protected void endSuper(State s) {
        ((HtmlState)s).target.append("</sup>");
        super.endSuper(s);
    }

    protected State startSub(State s) {
        ((HtmlState)s).target.append("<sub>");
        return super.startSub(s);
    }

    protected void endSub(State s) {
        ((HtmlState)s).target.append("</sub>");
        super.endSub(s);
    }

    protected State startCo(State s) {
        ((HtmlState)s).target
                .append("</td><td style=\"border-top:1px solid; margin-right:-5; margin-top:2; margin-left:-5;\">");
        return super.startCo(s);
    }

    protected void endCo(State s) {
        ((HtmlState)s).target.append("</td><td style=\"margin-top:2;\">");
        super.endCo(s);
    }

    protected void drawPlainString(State state, String str) {
        ((HtmlState)state).target.append(str);
    }

    protected void drawGlyph(State state, LatexGlyph g) {
        StringBuffer t = ((HtmlState)state).target;
        if (!g.getHtml().equals("")) {
            t.append(g.getHtml());
        } else {
            t.append("<img src=\"");
            t.append(imgpath);
            t.append(g.getImageName());
            t.append("\" alt=\"");
            t.append(g.getName());
            t.append("\"/>");
        }
    }

    protected class HtmlState extends Latex.State {
        protected StringBuffer target;

        public HtmlState(String s) {
            super(s);
            target = new StringBuffer();
        }

        public HtmlState(State parent) {
            super(parent);
            this.target = ((HtmlState)parent).target;
        }

        /**
         * Derive a new state and return it.
         */
        public State deriveStart() {
            return new HtmlState(this);
        }
    }

    
    
    /*
     * This method truncates the HTML string for the Node Label
     * It is called to truncate to 40 Characters and without suffix, since the default suffix is "..."
     * 
     */
    public static String truncateHTML(String text, int length, String suffix) {
        // if the plain text is shorter than the maximum length, return the
        // whole text
        if (text.replaceAll("<.*?>", "").length() <= length) {
            return text;
        }
        String result = "";
        boolean trimmed = false;
        if (suffix == null) {
            suffix = "...";
        }

        /*
         * This pattern creates tokens, where each line starts with the tag. For
         * example, "One, <b>Two</b>, Three" produces the following: One, <b>Two
         * </b>, Three
         */
        Pattern tagPattern = Pattern.compile("(<.+?>)?([^<>]*)");

        /*
         * Checks for an empty tag, for example img, br, etc.
         */
        Pattern emptyTagPattern = Pattern
                .compile("^<\\s*(img|br|input|hr|area|base|basefont|col|frame|isindex|link|meta|param).*>$");

        /*
         * Modified the pattern to also include H1-H6 tags Checks for closing
         * tags, allowing leading and ending space inside the brackets
         */
        Pattern closingTagPattern = Pattern
                .compile("^<\\s*/\\s*([a-zA-Z]+[1-6]?)\\s*>$");

        /*
         * Modified the pattern to also include H1-H6 tags Checks for opening
         * tags, allowing leading and ending space inside the brackets
         */
        Pattern openingTagPattern = Pattern
                .compile("^<\\s*([a-zA-Z]+[1-6]?).*?>$");

        /*
         * Find &nbsp; &gt; ...
         */
        Pattern entityPattern = Pattern
                .compile("(&[0-9a-z]{2,8};|&#[0-9]{1,7};|[0-9a-f]{1,6};)");

        // splits all html-tags to scanable lines
        Matcher tagMatcher = tagPattern.matcher(text);
        @SuppressWarnings("unused")
        int numTags = tagMatcher.groupCount();

        int totalLength = suffix.length();
        List<String> openTags = new ArrayList<String>();

        boolean proposingChop = false;
        while (tagMatcher.find()) {
            String tagText = tagMatcher.group(1);
            String plainText = tagMatcher.group(2);

            if (proposingChop && tagText != null && tagText.length() != 0
                    && plainText != null && plainText.length() != 0) {
                trimmed = true;
                break;
            }

            // if there is any html-tag in this line, handle it and add it
            // (uncounted) to the output
            if (tagText != null && tagText.length() > 0) {
                boolean foundMatch = false;

                // if it's an "empty element" with or without xhtml-conform
                // closing slash
                Matcher matcher = emptyTagPattern.matcher(tagText);
                if (matcher.find()) {
                    foundMatch = true;
                    // do nothing
                }

                // closing tag?
                if (!foundMatch) {
                    matcher = closingTagPattern.matcher(tagText);
                    if (matcher.find()) {
                        foundMatch = true;
                        // delete tag from openTags list
                        String tagName = matcher.group(1);
                        openTags.remove(tagName.toLowerCase());
                    }
                }

                // opening tag?
                if (!foundMatch) {
                    matcher = openingTagPattern.matcher(tagText);
                    if (matcher.find()) {
                        // add tag to the beginning of openTags list
                        String tagName = matcher.group(1);
                        openTags.add(0, tagName.toLowerCase());
                    }
                }

                // add html-tag to result
                result += tagText;
            }

            // calculate the length of the plain text part of the line; handle
            // entities (e.g. &nbsp;) as one character
            int contentLength = plainText.replaceAll(
                    "&[0-9a-z]{2,8};|&#[0-9]{1,7};|[0-9a-f]{1,6};", " ")
                    .length();
            if (totalLength + contentLength > length) {
                // the number of characters which are left
                int numCharsRemaining = length - totalLength;
                int entitiesLength = 0;
                Matcher entityMatcher = entityPattern.matcher(plainText);
                while (entityMatcher.find()) {
                    String entity = entityMatcher.group(1);
                    if (numCharsRemaining > 0) {
                        numCharsRemaining--;
                        entitiesLength += entity.length();
                    } else {
                        // no more characters left
                        break;
                    }
                }

                // keep us from chopping words in half
                int proposedChopPosition = numCharsRemaining + entitiesLength;
                int endOfWordPosition = plainText.indexOf(" ",
                        proposedChopPosition - 1);
                if (endOfWordPosition == -1) {
                    endOfWordPosition = plainText.length();
                }
                int endOfWordOffset = endOfWordPosition - proposedChopPosition;
                if (endOfWordOffset > 6) { // chop the word if it's extra long
                    endOfWordOffset = 0;
                }

                proposedChopPosition = numCharsRemaining + entitiesLength
                        + endOfWordOffset;
                if (plainText.length() >= proposedChopPosition) {
                    result += plainText.substring(0, proposedChopPosition);
                    proposingChop = true;
                    if (proposedChopPosition < plainText.length()) {
                        trimmed = true;
                        break; // maximum length is reached, so get off the loop
                    }
                } else {
                    result += plainText;
                }
            } else {
                result += plainText;
                totalLength += contentLength;
            }
            // if the maximum length is reached, get off the loop
            if (totalLength >= length) {
                trimmed = true;
                break;
            }
        }

        for (String openTag : openTags) {
            result += "</" + openTag + ">";
        }
        if (trimmed) {
            result = result.replace("</td></tr></table></div>",
                    "</td><td style=\"margin-top:2;\">" + suffix
                            + "</td></tr></table></div>");
        }
        return result;
    }

}

/* EOF */
