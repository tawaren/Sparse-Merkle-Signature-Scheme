package suit.config.interfaces;


import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.ILeafCalc;

public interface ILeafCalcConf {
    public ILeafCalc genCalc(IHashFunction h);
}
