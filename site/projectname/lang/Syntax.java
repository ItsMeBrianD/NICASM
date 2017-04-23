package site.projectname.lang;

/**
 * Syntax definition for a given language or dataset, used heavily by {@link site.projectname.err.SyntaxErrorException SyntaxErrorException}
 * <p>
 * Standards for implementation extend beyond a normal interface:
 * <ul>
 *  <li>Must be implemented by an Enum</li>
 *  <li>Should contain an enum with no parameters, used for passing enum as a static class (to SyntaxErrorException)</li>
 *  <li>Enum values should contain a regular expression defining the token</li>
 *  <li>Enum values should contain a semantic representation of the token, for error output</li>
 * </ul>
 *
 */
public interface Syntax {
    /**
     *  Returns a string representation of the token. This should be the regular expression
     *  @return     Regular Expression of the token
     */
    public String toString();
    /**
     *  Returns the short name of the token, i.e. HEX5 vs. 5-bit Hexidecimal value
     *  @return     Short name of token
     */
    public String getName();
    /**
     * Creates a set of possible solutions to an error
     * <p>
     * For example:
     * Input: (REG)|(HEX5|IMM5) (After evaluation to regular expression)
     * Output: Register or 5-bit Immediate value
     */
    public String getPossibles(String s);
    /**
     * Checks if the syntax contains a given regular expression
     * @return      Syntax contains regular expression
     */
    public boolean contains(String s);
    /**
     * Checks for Syntax-Specific errors.
     * <p>
     * For example {@link site.projetname.nicasm.NICSyntax NICSyntax} uses errorCheck to ensure tokens are comma-seperated, as {@link site.projectname.err.SyntaxErrorException SyntaxErrorException} disregards commas as of writing
     * @return Error Message regarding syntax-specific errors
     */
    public String errorCheck(String line, String message);
}
