package site.projectname.lang;

public interface Syntax {
    public String toString();
    public String getName();
    public String getPossibles(String s);
    public String getSemantic(String s);
    public boolean contains(String s);
}
