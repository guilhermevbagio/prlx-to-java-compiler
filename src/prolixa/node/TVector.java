/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TVector extends Token
{
    public TVector()
    {
        super.setText("vector");
    }

    public TVector(int line, int pos)
    {
        super.setText("vector");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TVector(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTVector(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TVector text.");
    }
}
