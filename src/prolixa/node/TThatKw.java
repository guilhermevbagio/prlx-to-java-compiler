/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TThatKw extends Token
{
    public TThatKw()
    {
        super.setText("that");
    }

    public TThatKw(int line, int pos)
    {
        super.setText("that");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TThatKw(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTThatKw(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TThatKw text.");
    }
}
