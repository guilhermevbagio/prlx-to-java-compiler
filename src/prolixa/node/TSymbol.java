/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TSymbol extends Token
{
    public TSymbol()
    {
        super.setText("symbol");
    }

    public TSymbol(int line, int pos)
    {
        super.setText("symbol");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TSymbol(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTSymbol(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TSymbol text.");
    }
}
