package green.dividendfinder.model.constants;

public enum Month {

    JAN("Jan", 1),
    FEB("Feb", 2),
    MAR("Mar", 3),
    APR("Apr", 4),
    MAY("May", 5),
    JUN("Jun", 6),
    JUL("Jul", 7),
    AUG("Aug", 8),
    SEP("Sep", 9),
    OCT("Oct", 10),
    NOV("Nov", 11),
    DEC("Dec", 12);

    private String str;
    private int number;

    Month(String str, int number) {
        this.str = str;
        this.number = number;
    }

    // 파싱된 Month 정보(String형)를 정수형으로 변환
    public static int strToNumber(String s) {
        for (var m : Month.values()) {
            if (m.str.equals(s)) {
                return m.number;
            }
        }

        return -1;
    }
}
