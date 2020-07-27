package utils;

public class LambdaCounter {
    public static final int COUNTER_INIT = 0;
    int counter;
    public LambdaCounter()
    {
        counter = COUNTER_INIT;
    }
    public LambdaCounter(int initCounter)
    {
        counter = initCounter;
    }

    public void Increment()
    {
        counter++;
    }

    public int getCounter()
    {
        return counter;
    }

}
