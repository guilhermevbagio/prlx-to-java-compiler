/* This file was generated by SableCC (http://www.sablecc.org/). */

package prolixa.node;

import prolixa.analysis.*;

@SuppressWarnings("nls")
public final class TAtribuicao extends Token
{
    public TAtribuicao()
    {
        super.setText(":=");
    }

    public TAtribuicao(int line, int pos)
    {
        super.setText(":=");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TAtribuicao(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTAtribuicao(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TAtribuicao text.");
    }
}
