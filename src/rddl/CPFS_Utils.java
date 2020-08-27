package rddl;

import convert.PLP_Converter;
import plp.PLP;
import plp.PLP_Achieve;
import plp.PLP_Observe;
import plp.objects.ProbabilityType;

import java.util.ArrayList;

public class CPFS_Utils {
    static boolean IS_ACHIEVE_SUCCESS_OBSERVABLE = true;//activating an action will return an observation telling if it was successful.

    public static String Get_CPFS_For_ActionSuccess(PLP plp, ProbabilityType actionSuccessProb, boolean isProbabilityOfSuccess) {
        float successProb = isProbabilityOfSuccess ? actionSuccessProb.GetConditionalProbabilities()[0].Probability
                : 1 - actionSuccessProb.GetConditionalProbabilities()[0].Probability;
        String res = PLP_Converter.GetActionSuccessIntermName(plp) +
                "=" +
                "if(" + PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams()) +
                " (" + plp.PlpNameWithParams(false) + ")" +
                ") then Bernoulli(" + successProb + ")" +
                " else false;";
        return res;
    }

    private static String Get_CPFS_ObservationValue(PLP plp) {
        if(plp instanceof PLP_Observe)
        {
            PLP_Observe po = (PLP_Observe)plp;
            return "KronDelta(" + PLP_Converter.GetActionSuccessIntermName(plp) +
                    " + (" +
                    GetExistObserveAction_AndObservedPredicateValue(po) +
                    "^" + PLP_Converter.GetActionSuccessIntermName(plp) +
                    "^" + "Bernoulli(" + po.probabilityGivenObservedValue.GetConditionalProbabilities()[0].Probability + ")))";
        }
        if(plp instanceof PLP_Achieve) {
            if (IS_ACHIEVE_SUCCESS_OBSERVABLE) {
                return "KronDelta(" + PLP_Converter.GetActionSuccessIntermName(plp) + " + 0)";
            }else {
                return "KronDelta(0)";
            }
        }

        throw new UnsupportedOperationException("This PLP Type is not supporting observations");
    }

  public static ArrayList<String> Get_CPFS_For_Observations(ArrayList<PLP> plps) {
      ArrayList<String> lines = new ArrayList<>();

      if (plps != null) {
          for (int i = 0; i < plps.size(); i++) {
              if (plps.get(i) instanceof PLP_Achieve && !((PLP_Achieve) plps.get(i)).IsActionSuccessObservable) {
                  continue;
              }
              StringBuilder line = new StringBuilder(PLP_Converter.GetObservationValueName(plps.get(i)) + "=");
              line.append(Get_CPFS_ObservationValue(plps.get(i)));
              line.append(";");
              lines.add(line.toString());
          }
      }
        return lines;
  }


    /*public static String Get_CPFS_For_Observations(ArrayList<PLP> plps) {
        String res = "obsr_observation'=KronDelta(success_observe_can + \n" +
                "\t\t(exists_{?robot : robot, ?can : obj, ?location : discrete_location}observe_can(?robot,?can,?location) ^ \n" +
                "\t\tobject_at(?can,?location)^success_observe_can^Bernoulli(0.95))) + KronDelta(1);\n";

        String s3 = PLP_Converter.GetObservationValueName(plp) + "'=" +
                "KronDelta(" + PLP_Converter.GetActionSuccessIntermName(plp) +
                " + (" +
                GetExistObserveAction_AndObservedPredicateValue(plp) +
                "^" + PLP_Converter.GetActionSuccessIntermName(plp) +
                "^" + "Bernoulli(" + plp.probabilityGivenObservedValue.GetConditionalProbabilities()[0].Probability + ")));";
        return res;
    }*/

    public static String GetExistObserveAction_AndObservedPredicateValue(PLP_Observe plp)
    {
        return  PLP2RDDL_Utils.GetExistsForPredicate(plp.GetParams()) +
                plp.PlpNameWithParams(false) + " ^ " + plp.observationGoalPredicate.baseToString(true, true);
    }
}
