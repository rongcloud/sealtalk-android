package cn.rongcloud.im.common;

public class BrandMap {
    public static String getAbbrFromBrand(String brand) {
        String lowcaseBrand = brand.toLowerCase();
        switch (lowcaseBrand) {
            case "huawei":
                return "HW";
            case "honor":
                return "HONOR";
            case "ohos":
                return "OHOS";
            case "vivo":
                return "VIVO";
            case "oppo":
                return "OPPO";
            case "meizu":
                return "MEIZU";
            case "xiaomi":
                return "MI";
            default:
                return brand;
        }
    }
}
