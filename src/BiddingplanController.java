package cn.pms.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.pms.model.Page;
import cn.pms.model.TenderingCustom;
import cn.pms.pojo.Biddingplan;
import cn.pms.pojo.Expert;
import cn.pms.pojo.Tenderingheadinfo;
import cn.pms.pojo.User;
import cn.pms.pojo.Zbandzj;
import cn.pms.service.BiddingplanService;
import cn.pms.service.TenderingHeadInfoService;
import cn.pms.service.ZbandzjService;

@Controller
public class BiddingplanController {

	//招标方案表service
	@Autowired
	private BiddingplanService biddingplanService;
	//招标头表
	@Autowired
	private TenderingHeadInfoService tenderingHeadInfoService;
	//招标方案与专家关联关系表
	@Autowired
	private ZbandzjService zbandzjService;
	
	
	//招标方案制作表的模糊查询分页
	@RequestMapping("/findBiddingplan")
	public String findBiddingplan(HttpServletRequest request ,TenderingCustom tenderingCustom) {
		//System.out.println("执行了findBiddingplan");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (tenderingCustom.getBuyStartsTime() != null && tenderingCustom.getBuyStarteTime() != null 
				&& tenderingCustom.getBuyEndsTime() != null && tenderingCustom.getBuyEndeTime() != null &&
				tenderingCustom.getBuyStartsTime() != "" && tenderingCustom.getBuyStarteTime() != "" 
				&& tenderingCustom.getBuyEndsTime() != "" && tenderingCustom.getBuyEndeTime() != "") {
			try {
				tenderingCustom.setStartsTime(sdf.parse(tenderingCustom.getBuyStartsTime()));
				tenderingCustom.setStarteTime(sdf.parse(tenderingCustom.getBuyStarteTime()));
				tenderingCustom.setEndsTime(sdf.parse(tenderingCustom.getBuyEndsTime()));
				tenderingCustom.setEndeTime(sdf.parse(tenderingCustom.getBuyEndeTime()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		//得到前台页面上传来的值
		//得到总数据
		int totalRecordCount = biddingplanService.getCount(tenderingCustom);
		//System.out.println(totalRecordCount);
		//设置当前显示行数
		if (tenderingCustom.getPrePageRecordCount() == 0) {
			tenderingCustom.setPrePageRecordCount(5);
		}
		
		Page page = new Page(tenderingCustom.getCurrentPageIndex(), totalRecordCount, tenderingCustom.getPrePageRecordCount());
		
		tenderingCustom.setStratCount(page.getStratCount());
		
		List<TenderingCustom> tenderingCustomList = biddingplanService.findTenderingCustom(tenderingCustom);
		
		
		//格式化时间
		for (int i = 0; i < tenderingCustomList.size(); i++) {
			tenderingCustomList.get(i).setBuyStartsTime(sdf.format(tenderingCustomList.get(i).getStartTime()));
			tenderingCustomList.get(i).setBuyStarteTime(sdf.format(tenderingCustomList.get(i).getEndTime()));
		}
		//将数据丢给request
		
		request.setAttribute("tenderingCustom", tenderingCustom);
		request.setAttribute("tenderingCustomList", tenderingCustomList);
		request.setAttribute("page", page);
		
		return "ztbgl_zbfazz";
	}
	
	//批量删除，删除
	@RequestMapping("/deleteBiddingplan")
	@ResponseBody
	public String deleteBiddingplan(String ids) {
		
		String[] idList = ids.split(",");
		int j =0;
		for (int i = 0; i < idList.length; i++) {
			j += biddingplanService.deleteContract(idList[i]);
		}
		return ""+j;
	}
	
	
	//根据id去查看数据
	@RequestMapping("/findById")
	public String findById(HttpServletRequest request ,Integer id,Integer numb ) {
		Biddingplan biddingplan = biddingplanService.findById(id);
		Tenderingheadinfo tenderingheadinfo = tenderingHeadInfoService.findTenderingById(biddingplan.getTenderingHeadInfoId());
		//查询专家关联关系表
		List<Expert> expertList = zbandzjService.findByBiddingId(biddingplan.getId());
		//将数据传到页面上
		request.setAttribute("id", id);
		request.setAttribute("expertList", expertList);
		request.setAttribute("tenderingheadinfo", tenderingheadinfo);
		request.setAttribute("biddingplan", biddingplan);
		
		if (numb == 1) {
			return "ztbgl_zbfazz_detail";
		}else if(numb == 2){
			return "ztbgl_zbfazz_modification";
		}
		return "ztbgl_zbfazz_add";
	}
	//根据id去查看数据
		@RequestMapping("/findById2")
		public String findById2(HttpServletRequest request ,Integer id ) {
			//Biddingplan biddingplan = biddingplanService.findById(id);
			Tenderingheadinfo tenderingheadinfo = tenderingHeadInfoService.findTenderingById(id);
			//查询专家关联关系表
			//List<Expert> expertList = zbandzjService.findByBiddingId(biddingplan.getId());
			//将数据传到页面上
			request.setAttribute("id", id);
			//request.setAttribute("expertList", expertList);
			request.setAttribute("tenderingheadinfo", tenderingheadinfo);
			//request.setAttribute("biddingplan", biddingplan);
			
			return "ztbgl_zbfazz_add";
		}
	
	
	
	//根据id去修改数据
	@RequestMapping("/updateBidding")
	public String updateBidding(HttpServletRequest request,Biddingplan biddingplan) {
		
		int i = biddingplanService.updateBidding(biddingplan);
		
		return "forward:/findBiddingplan.action?currentPageIndex=1";
	}
	//增加数据
	@RequestMapping("/addBidding")
	public String addBidding(HttpServletRequest request,Biddingplan biddingplan,int[] expertId) {
		//得到session对象
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("loginUser");
		
		biddingplan.setCreateUser(user.getUserName());
		
		int i = biddingplanService.addBidding(biddingplan);
		
		int j = 0;
		if (i == 1 ) {
			for (int k = 0; k < expertId.length ; k++) {
				j += zbandzjService.addExpert(expertId[k],biddingplan.getId());
			}
		}
		return "ztbgl_zbfazz_add";
	}
	
	
	//删除专家与招标计划头表的关联关系
	@RequestMapping("/deleteExpert")
	@ResponseBody
	public String deleteExpert(String ids,Integer id,Integer count) {
		String[] idList = ids.split(",");
		int j =1;
		if (count == 1 ) {
			for (int i = 0; i < idList.length; i++) {
				j += zbandzjService.deleteExpert(idList[i],id);
			}
		}
		
		return ""+j;
	}
	
	//随机添加新的专家，不同的
	@RequestMapping("/addExpert")
	@ResponseBody
	public Map<String, Object> addExpert(String ids,Integer id,Integer count) {
		//ids是指已经存在的存在的专家，id是招标方案的id
		//去数据去得到在这个ids里面的专家数据
		ids = ids + 0;
		List<Expert> expertList = zbandzjService.ExpertList(ids);
		
		Random random = new Random();
		
		Expert expert = expertList.get(random.nextInt(expertList.size()));
		int j = 1;
		
		if (count == 1) {
			j = zbandzjService.addExpert(expert.getId(),id);
		}
		
		Map<String, Object> map =new HashMap<String, Object>();
		map.put("j", j);
		map.put("expert", expert);
		
		return map;
	}
	
	
}
