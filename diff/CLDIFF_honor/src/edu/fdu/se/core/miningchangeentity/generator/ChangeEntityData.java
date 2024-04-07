//package edu.fdu.se.core.miningchangeentity.generator;
//
//import com.github.gumtreediff.actions.model.Delete;
//import com.github.gumtreediff.actions.model.Insert;
//import edu.fdu.se.global.Global;
//import edu.fdu.se.core.links.linkbean.Link;
//import edu.fdu.se.core.miningactions.bean.MyRange;
//import edu.fdu.se.core.miningactions.bean.MiningActionData;
//import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
//import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
//import edu.fdu.se.core.miningchangeentity.member.*;
//import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
//import org.eclipse.jdt.core.dom.*;
//
//import java.util.List;
//
//
///**
// * Created by huangkaifeng on 2018/1/13.
// *
// */
//public class ChangeEntityData {
//
//    public String fileFullPackageName;
//
//    public MiningActionData mad;
//
//
//    public ChangeEntityData(MiningActionData mad) {
//        this.mad = mad;
//    }
//
//
//    public ChangeEntity addOneBody(BodyDeclarationPair item, String type) {
//        ChangeEntity ce = null;
//        int s;
//        int e;
//        MyRange myRange = null;
//        Object n = item.getBodyDeclaration();
//        if (Insert.class.getSimpleName().equals(type)) {
//            s = Global.astNodeUtil.getLineNumber(Global.astNodeUtil.getCurrCu(mad.preTrimData.getCurrCu()), Global.astNodeUtil.getStartPosition(n));
//            e = Global.astNodeUtil.getLineNumber(Global.astNodeUtil.getCurrCu(mad.preTrimData.getCurrCu()), Global.astNodeUtil.getStartPosition(n) + Global.astNodeUtil.getNodeLength(n));
//            myRange = new MyRange(s, e, ChangeEntityDesc.StageITreeType.CURR_TREE_NODE);
//        } else if (Delete.class.getSimpleName().equals(type)) {
//            s = Global.astNodeUtil.getLineNumber(Global.astNodeUtil.getPrevCu(mad.preTrimData.getPrevCu()), Global.astNodeUtil.getStartPosition(n));
//            e = Global.astNodeUtil.getLineNumber(Global.astNodeUtil.getPrevCu(mad.preTrimData.getPrevCu()), Global.astNodeUtil.getStartPosition(n) + Global.astNodeUtil.getNodeLength(n));
//            myRange = new MyRange(s, e, ChangeEntityDesc.StageITreeType.PREV_TREE_NODE);
//        }
//        if (Global.astNodeUtil.isFieldDeclaration(n)) {
//            ce = new FieldChangeEntity(item, type, myRange);
//        } else if (Global.astNodeUtil.isMethodDeclaration(n)) {
//            ce = new MethodChangeEntity(item, type, myRange);
//        } else if (item.getBodyDeclaration() instanceof Initializer) {  //Java专用
//            ce = new InitializerChangeEntity(item, type, myRange);
//        } else if (Global.astNodeUtil.isTypeDeclaration(n)) {
//            ce = new ClassChangeEntity(item, type, myRange, 0);
//        } else if (Global.astNodeUtil.isEnumDeclaration(n)) {
//            ce = new EnumChangeEntity(item, type, myRange);
//        }
//        return ce;
//    }
//
//
//
//
//
//
//}
