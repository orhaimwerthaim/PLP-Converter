package plp.objects.effect;

import org.w3c.dom.Node;

public class ForAllEffect implements IEffect {
    private EEffectingUpon effectEffectingUpon = null;

    public void setEffectEffectingUpon(EEffectingUpon effectingUpon)
    {
        effectEffectingUpon = effectingUpon;
    }

    @Override
    public EEffectingUpon getEffectingUpon() {
        return effectEffectingUpon;
    }

    @Override
    public void setEffectingUpon(EEffectingUpon upon) {
        effectEffectingUpon = upon;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getEffectAssignedValue() {
        return null;
    }

    public ForAllEffect(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof ForAllEffect)) return false;
        return ((NotEffect)obj).toString().equals(toString());
    }
}
