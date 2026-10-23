package com.edu.crimepredictor;

import java.util.*;

public class CrimeRiskCalculator {

    public static class AreaInfo {
        public final String name;
        public final String province;
        public final double baselineRisk;

        public AreaInfo(String name, String province, double baselineRisk) {
            this.name = name;
            this.province = province;
            this.baselineRisk = baselineRisk;
        }
    }

    public static class Factor {
        public final String name;
        public final int percentage;

        public Factor(String name, int percentage) {
            this.name = name;
            this.percentage = percentage;
        }
    }

    public static class NearbyArea {
        public final String name;
        public final int score;
        public final String level;

        public NearbyArea(String name, int score, String level) {
            this.name = name;
            this.score = score;
            this.level = level;
        }
    }

    public static class CalculationResult {
        public final int score;
        public final String level;
        public final String confidence;
        public final String summary;
        public final String safetyTip;
        public final List<Factor> factors;
        public final int[] hourlyScores; // 24 hours
        public final int peakHour;
        public final int peakScore;
        public final List<NearbyArea> nearbyAreas;
        public final String province;

        public CalculationResult(int score, String level, String confidence, String summary,
                                 String safetyTip, List<Factor> factors, int[] hourlyScores,
                                 int peakHour, int peakScore, List<NearbyArea> nearbyAreas,
                                 String province) {
            this.score = score;
            this.level = level;
            this.confidence = confidence;
            this.summary = summary;
            this.safetyTip = safetyTip;
            this.factors = factors;
            this.hourlyScores = hourlyScores;
            this.peakHour = peakHour;
            this.peakScore = peakScore;
            this.nearbyAreas = nearbyAreas;
            this.province = province;
        }
    }

    // South African Areas with Province and Baseline Risk
    public static final Map<String, AreaInfo> AREAS = new LinkedHashMap<>();
    static {
        // Gauteng
        addArea("Tembisa", "Gauteng", 0.90);
        addArea("Soweto", "Gauteng", 0.88);
        addArea("Alexandra", "Gauteng", 0.90);
        addArea("Johannesburg CBD", "Gauteng", 0.92);
        addArea("Pretoria CBD", "Gauteng", 0.85);
        addArea("Mamelodi", "Gauteng", 0.82);
        addArea("Soshanguve", "Gauteng", 0.80);
        addArea("Sandton", "Gauteng", 0.60);
        addArea("Centurion", "Gauteng", 0.50);
        // Western Cape
        addArea("Khayelitsha", "Western Cape", 0.90);
        addArea("Nyanga", "Western Cape", 0.95);
        addArea("Mitchells Plain", "Western Cape", 0.85);
        addArea("Cape Town CBD", "Western Cape", 0.75);
        // KwaZulu-Natal
        addArea("Umlazi", "KwaZulu-Natal", 0.82);
        addArea("Inanda", "KwaZulu-Natal", 0.80);
        addArea("Durban CBD", "KwaZulu-Natal", 0.80);
        addArea("Pietermaritzburg", "KwaZulu-Natal", 0.65);
        // Eastern Cape
        addArea("Mdantsane", "Eastern Cape", 0.75);
        addArea("Gqeberha North End", "Eastern Cape", 0.70);
        addArea("Mthatha", "Eastern Cape", 0.70);
        // Free State
        addArea("Bloemfontein CBD", "Free State", 0.60);
        addArea("Mangaung", "Free State", 0.55);
        // Limpopo
        addArea("Polokwane", "Limpopo", 0.45);
        addArea("Thohoyandou", "Limpopo", 0.40);
        // Mpumalanga
        addArea("Mbombela", "Mpumalanga", 0.55);
        addArea("Emalahleni", "Mpumalanga", 0.55);
        // North West
        addArea("Rustenburg", "North West", 0.60);
        addArea("Mahikeng", "North West", 0.50);
        // Northern Cape
        addArea("Kimberley", "Northern Cape", 0.50);
        addArea("Upington", "Northern Cape", 0.40);
    }

    private static void addArea(String name, String province, double baseline) {
        AREAS.put(name.toLowerCase(), new AreaInfo(name, province, baseline));
    }

    public static AreaInfo getAreaInfo(String inputArea) {
        if (inputArea == null || inputArea.trim().isEmpty()) {
            return new AreaInfo("Specified Area", "Unknown", 0.50);
        }
        String key = inputArea.trim().toLowerCase();
        if (AREAS.containsKey(key)) {
            return AREAS.get(key);
        }
        for (Map.Entry<String, AreaInfo> entry : AREAS.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new AreaInfo(inputArea.trim(), "Other", 0.50);
    }

    public static String[] getAllAreaNames() {
        String[] names = new String[AREAS.size()];
        int idx = 0;
        for (AreaInfo info : AREAS.values()) {
            names[idx++] = info.name;
        }
        return names;
    }

    // User-specified Crime Types
    public static final String[] CRIME_TYPES = {
            "murder",
            "attempted murder",
            "rape",
            "aggravated robbery",
            "residential burglary",
            "drug related crime",
            "assault"
    };

    public static final double[] CRIME_WEIGHTS = { 0.95, 0.90, 0.90, 0.85, 0.75, 0.70, 0.80 };

    public static final String[] CRIME_TIPS = {
            "Avoid isolated areas after dark and report suspicious behavior to law enforcement immediately.",
            "Stay in well-lit public areas, travel in groups, and remain vigilant of your surroundings.",
            "Avoid walking alone at night, stick to trusted routes, and keep emergency contacts handy.",
            "Keep phone and valuables out of sight, avoid ATMs at night, and stay alert at traffic signals.",
            "Ensure home windows and doors are locked, keep outdoor lighting on, and alert trusted neighbors.",
            "Avoid known drug hotspot areas and report illegal activities to local authorities.",
            "Avoid confrontations in crowded or drinking venues and remain in well-lit, populated areas."
    };

    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    public static final double[] DAY_WEIGHTS = { 0.60, 0.55, 0.55, 0.65, 0.85, 0.95, 0.80 };

    public static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    public static final double[] MONTH_WEIGHTS = {
            0.85, 0.70, 0.65, 0.65, 0.60, 0.60, 0.65, 0.65, 0.70, 0.75, 0.80, 0.95
    };

    public static double getHourWeight(int hour) {
        int dist = Math.min(Math.abs(hour - 21), 24 - Math.abs(hour - 21));
        return 0.35 + 0.65 * Math.max(0.0, 1.0 - ((double) dist / 8.0));
    }

    public static String getRiskLevel(int score) {
        if (score < 30) return "Low";
        if (score < 55) return "Moderate";
        if (score < 75) return "High";
        return "Critical";
    }

    public static CalculationResult calculate(String areaName, String crimeType, int hour,
                                             int dayIndex, int monthIndex, int socioEconomicIndex,
                                             int recentCommunityReports) {
        AreaInfo areaInfo = getAreaInfo(areaName);
        double baseline = areaInfo.baselineRisk;

        int crimeIdx = 0;
        for (int i = 0; i < CRIME_TYPES.length; i++) {
            if (CRIME_TYPES[i].equalsIgnoreCase(crimeType)) {
                crimeIdx = i;
                break;
            }
        }
        double crimeWeight = CRIME_WEIGHTS[crimeIdx];
        String tip = CRIME_TIPS[crimeIdx];

        int d = Math.max(0, Math.min(6, dayIndex));
        int m = Math.max(0, Math.min(11, monthIndex));
        double dWeight = DAY_WEIGHTS[d];
        double mWeight = MONTH_WEIGHTS[m];
        double seWeight = Math.max(0, Math.min(100, socioEconomicIndex)) / 100.0;
        double crWeight = Math.min(1.0, (double) recentCommunityReports / 4.0);

        // Hourly curve
        int[] hourlyScores = new int[24];
        int peakHour = 0;
        int peakScore = -1;

        for (int h = 0; h < 24; h++) {
            double hWeight = getHourWeight(h);
            double weightedSum = (baseline * 0.30) +
                                 (hWeight * 0.20) +
                                 (dWeight * 0.13) +
                                 (mWeight * 0.11) +
                                 (seWeight * 0.16) +
                                 (crWeight * 0.10);
            int s = (int) Math.round(Math.min(99, Math.max(3, weightedSum * crimeWeight * 115.0)));
            hourlyScores[h] = s;
            if (s > peakScore) {
                peakScore = s;
                peakHour = h;
            }
        }

        int score = hourlyScores[hour];
        String level = getRiskLevel(score);

        String confidence;
        if ("Other".equals(areaInfo.province)) {
            confidence = "Low (unlisted area)";
        } else if (Math.abs(socioEconomicIndex - 50) > 25) {
            confidence = "High";
        } else {
            confidence = "Moderate";
        }

        String summary = String.format("%s in %s, %s %02d:00, %s.",
                CRIME_TYPES[crimeIdx], areaInfo.name, DAYS[d], hour, MONTHS[m]);

        String fullTip = (score >= 55 ? "Elevated risk window. " : "Relatively low risk, but stay aware. ") + tip;

        List<Factor> factors = new ArrayList<>();
        factors.add(new Factor("Area baseline", (int) Math.round(baseline * 100)));
        factors.add(new Factor("Time of day", (int) Math.round(getHourWeight(hour) * 100)));
        factors.add(new Factor("Day of week", (int) Math.round(dWeight * 100)));
        factors.add(new Factor("Seasonality", (int) Math.round(mWeight * 100)));
        factors.add(new Factor("Socio-economic index", (int) Math.round(seWeight * 100)));
        factors.add(new Factor("Community reports", (int) Math.round(crWeight * 100)));

        // Nearby / Provincial ranking
        List<NearbyArea> nearby = new ArrayList<>();
        if (!"Other".equals(areaInfo.province)) {
            for (AreaInfo other : AREAS.values()) {
                if (other.province.equalsIgnoreCase(areaInfo.province)) {
                    double otherWeighted = (other.baselineRisk * 0.30) +
                                           (getHourWeight(hour) * 0.20) +
                                           (dWeight * 0.13) +
                                           (mWeight * 0.11) +
                                           (seWeight * 0.16) +
                                           (crWeight * 0.10);
                    int s = (int) Math.round(Math.min(99, Math.max(3, otherWeighted * crimeWeight * 115.0)));
                    nearby.add(new NearbyArea(other.name, s, getRiskLevel(s)));
                }
            }
            nearby.sort((a, b) -> Integer.compare(b.score, a.score));
            if (nearby.size() > 5) {
                nearby = nearby.subList(0, 5);
            }
        }

        return new CalculationResult(score, level, confidence, summary, fullTip, factors,
                hourlyScores, peakHour, peakScore, nearby, areaInfo.province);
    }
}
