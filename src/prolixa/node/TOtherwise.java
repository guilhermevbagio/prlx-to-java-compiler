/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TOtherwise extends Token
{
    public TOtherwise()
    {
        super.setText("otherwise");
    }

    public TOtherwise(int line, int pos)
    {
        super.setText("otherwise");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TOtherwise(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTOtherwise(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TOtherwise text.");
    }
}
