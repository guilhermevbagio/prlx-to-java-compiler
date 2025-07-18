/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TPresents extends Token
{
    public TPresents()
    {
        super.setText("presents");
    }

    public TPresents(int line, int pos)
    {
        super.setText("presents");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TPresents(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTPresents(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TPresents text.");
    }
}
