import edu.fdu.se.global.Global;
import main.CLDIFFCmd;

public class RQ4_Humanstudy {


    public static void main(String args[]) throws Exception{
        String[] itemList = {
                "spring-framework__fdse__3c1adf7f6af0dff9bda74f40dabe8cf428a62003",
                "rocketmq__fdse__b421d48c476e74a8c7bb8129979df1dc0cb5a5a5",
                "rocketmq__fdse__85467dfd34d8ff379d2ddfec0489d78dcba20c27",
                "rocketmq__fdse__f508f131f7dee2bcb86e66a6beb7bbdedbe31bc6",
                "rocketmq__fdse__b1fcf1b83b659bd03bcebf651d9e88c294a89e07",
                "rocketmq__fdse__e9a0d62dbd8a1a6163870828d583300eb27e324a",
                "rocketmq__fdse__3292d039ae882f42168d5f98969df83130ca05fb",
                "rocketmq__fdse__81884c8df3009a01c18dcfbf83a5109831878527",
                "rocketmq__fdse__37cf2a7cb706a0cfe5b217940467c4a9cb33626e",
//                "rocketmq__fdse__81507467b88bc14a85d4ace97225fce27f3b712d",
                "rocketmq__fdse__c94fc4fa562ddc20332c3add01b95e291cd325a3",
                "rocketmq__fdse__c67413749d60682a3393f824487da0478311dd0a",
                "rocketmq__fdse__d8c446e854e6cce1b54c0d9d97f3189832b88001",
                "rocketmq__fdse__46f91479d95186e171b7ac43c9639972354e7ff2",
                "rocketmq__fdse__0f76489882d3954fd640e28c7b8704b6d9f3096f",
                "rocketmq__fdse__9f4934fc9846ed151aa6bb20b7e971ee9a043a32",
                "rocketmq__fdse__9c0e5360e109b2a5c4c86ed7053a59f868b078ee",
                "rocketmq__fdse__8422e74fa5d1f6c8a29770e568d9bb228ac1f6c7",
                "rocketmq__fdse__1970813ceb0dab2e96d26cef75666bd71bb89ca8",
                "rocketmq__fdse__92f0e1f5abb694c90ced9468e9e1d03edcdcedfe",
                "dbeaver__fdse__2dfe3f245844b9edf5021808d7278cf1f27ac312",
                "dubbo__fdse__d56eaa8ac9e8eaab69e42124ae82bd7de42b8d74",
                "dubbo__fdse__72fe93bf22c1f7db88b5d083c9570b27e1032542",
                "rocketmq__fdse__0b39fcadfa2950f1dd3975e1262ccd544f350750",
                "rocketmq__fdse__c79614071b1941940f934c066bde5711062bdc7e"
        };
        for(int i =0;i < 9;i++) {
            String item = itemList[i];
            String[] data = item.split("__fdse__");
            String proj = data[0];
            String commitId = data[1];
            String repo = String.format("/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/projects/%s/.git",proj);
            System.out.println(proj);
            System.out.println(commitId);
            String outputDir = "/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/evaluation-humanstudy";
            String[] a = {repo, commitId, outputDir};
            CLDIFFCmd.main(a);
//            break;
        }

    }
}
