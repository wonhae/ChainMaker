package kr.happyjob.chainmaker.scm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.happyjob.chainmaker.scm.dao.DailyOrderHistoryDao;
import kr.happyjob.chainmaker.scm.model.DailyOrderListDTO;
import kr.happyjob.chainmaker.scm.model.DailyOrderListVO;
import kr.happyjob.chainmaker.scm.model.OrderDetailByOrderNoAndProNoDTO;
import kr.happyjob.chainmaker.scm.model.OrderDetailByOrderNoAndProNoVO;
import kr.happyjob.chainmaker.scm.model.OrdersRequestDTO;
import kr.happyjob.chainmaker.scm.model.ProductDetailDTO;
import kr.happyjob.chainmaker.scm.model.ProductDetailVO;
import kr.happyjob.chainmaker.scm.model.PurchaseAndShippingInfoDTO;
import kr.happyjob.chainmaker.scm.model.PurchaseInfoDTO;
import kr.happyjob.chainmaker.scm.model.ShippingDirectionDTO;
import kr.happyjob.chainmaker.scm.model.WHInfoByProNoDTO;
import kr.happyjob.chainmaker.scm.model.WHInfoByProNoVO;

@Transactional
@Service("DailyOrderHistoryServiceImpl")
public class DailyOrderHistroyServiceImpl implements DailyOrderHistoryService{
	
	private final Logger logger = LogManager.getLogger(this.getClass());
	
	@Autowired
	private DailyOrderHistoryDao dailyOrderHistoryDao;

	@Override
	public List<DailyOrderListDTO> getDailyOrderListByOrderCD(OrdersRequestDTO pageOrderInfoDTO) {
		List<DailyOrderListVO> dailyOrderVOList = dailyOrderHistoryDao.selectDailyOrderListByOrderCD(pageOrderInfoDTO);
		
		Iterator<DailyOrderListVO> dailyOrderVOListIterator = dailyOrderVOList.iterator();

		List<DailyOrderListDTO> dailyOrderDTOList = new LinkedList<>();
		
		while(dailyOrderVOListIterator.hasNext()) {
			DailyOrderListVO dailyOrderVO = dailyOrderVOListIterator.next();
			
			DailyOrderListDTO dailyOrderDTO = new DailyOrderListDTO(dailyOrderVO);
			
			dailyOrderDTOList.add(dailyOrderDTO);
		}
		
		return dailyOrderDTOList;
	}

	@Override
	public int countDailyOrderListByOrderCD(OrdersRequestDTO orderRequestDTO) {
		return dailyOrderHistoryDao.countListDailyOrderByOrderCD(orderRequestDTO);
	}

	@Override
	public OrderDetailByOrderNoAndProNoDTO selectOrderDetailByOrderNoAndProNo(OrdersRequestDTO pageOrderInfoDTO) {
		
		int order_no = pageOrderInfoDTO.getOrder_no();
		String pro_no = pageOrderInfoDTO.getPro_no();
		System.out.println("service:"+order_no+" a"+pro_no);
		
		OrderDetailByOrderNoAndProNoVO orderDetailVO = dailyOrderHistoryDao.selectOrderDetailByOrderNoAndProNo(pageOrderInfoDTO);
		

		System.out.println("VO:"+orderDetailVO.getOrder_no()+" a"+orderDetailVO.getPro_no());
		
		
		OrderDetailByOrderNoAndProNoDTO orderDetailDTO = new OrderDetailByOrderNoAndProNoDTO(orderDetailVO);
		
		
		return orderDetailDTO;
	}

	@Override
	public List<WHInfoByProNoDTO> selectWHInfoByProNo(String pro_no) {
		
		List<WHInfoByProNoVO> whInfoByProNoVOList = dailyOrderHistoryDao.selectWHInfoByProNo(pro_no);
		
		Iterator<WHInfoByProNoVO> whInfoIterator = whInfoByProNoVOList.iterator();
		
		List<WHInfoByProNoDTO> whInfoByProNoDTOList = new LinkedList<>();
		
		while(whInfoIterator.hasNext()) {
			WHInfoByProNoVO whInfoByProNoVO = whInfoIterator.next();
			
			WHInfoByProNoDTO whInfoByProNoDTO = new WHInfoByProNoDTO(whInfoByProNoVO);
			
			whInfoByProNoDTOList.add(whInfoByProNoDTO);
			
		}
		
		return whInfoByProNoDTOList;
	}

	@Override
	public int createShippingInfoReturnShipNo(List<PurchaseAndShippingInfoDTO> purchaseAndShippingInfoDTOList, String writerID) {
		
		List<ShippingDirectionDTO> shippingDirectionDTOList = new ArrayList<>();
		
		Iterator<PurchaseAndShippingInfoDTO> purchaseAndShippingInfoDTOIterator = purchaseAndShippingInfoDTOList.iterator();
		
		while(purchaseAndShippingInfoDTOIterator.hasNext()) {
			PurchaseAndShippingInfoDTO purchaseAndShippingInfoDTO = purchaseAndShippingInfoDTOIterator.next();
			ShippingDirectionDTO shippingDirectionDTO = new ShippingDirectionDTO(purchaseAndShippingInfoDTO);
			shippingDirectionDTOList.add(shippingDirectionDTO);
		}
		
		
		// ?????? ??? ship key??? ???????????????
		ShippingDirectionDTO firstShippingDriectionDTO = shippingDirectionDTOList.remove(0);
		int shipNo = 0;

		Map<String, Object> map = new HashMap<>();
		
		try {
			// 1?????? ??????
			if(shippingDirectionDTOList.isEmpty()) {

				dailyOrderHistoryDao.insertShippingInfo(firstShippingDriectionDTO);
				shipNo = firstShippingDriectionDTO.getShip_no();
				
				dailyOrderHistoryDao.updateOrderCDtoComplete(firstShippingDriectionDTO);
				
				firstShippingDriectionDTO.setWriter_id(writerID);
				
				// ?????? ????????? ??????
				dailyOrderHistoryDao.insertShippingDirection(firstShippingDriectionDTO);
				
				// out_pre ??????
				
				shippingDirectionDTOList.add(firstShippingDriectionDTO);
				
				map.put("shippingDirectionDTOList", shippingDirectionDTOList);
				
				String pro_io_cd = "out_pre";
				
				String pro_io_memo = "?????? ???????????? ?????? ?????? ??????";
				
				String order_no_flag = "on";
				
				map.put("pro_io_cd",pro_io_cd);
				map.put("pro_io_memo",pro_io_memo);
				map.put("order_no_flag",order_no_flag);
				
				dailyOrderHistoryDao.insertProductIOWarehouse(map);
				
				
			} else {
				dailyOrderHistoryDao.insertShippingInfo(firstShippingDriectionDTO);
				shipNo = firstShippingDriectionDTO.getShip_no();
				
				logger.info("??? ship_no : " + shipNo);
				
				
				for(ShippingDirectionDTO shippingDirectionDTO : shippingDirectionDTOList) {
					shippingDirectionDTO.setShip_no(shipNo);
				}
				map.put("shippingDirectionDTOList", shippingDirectionDTOList);
				
				dailyOrderHistoryDao.insertShippingInfoList(map);
				
				dailyOrderHistoryDao.updateOrderCDtoComplete(firstShippingDriectionDTO);
				
				// ?????? ????????? ?????? ?????? ?????? ??? ?????? ??????
				shippingDirectionDTOList.add(firstShippingDriectionDTO);

				for(ShippingDirectionDTO shippingDirectionDTO : shippingDirectionDTOList) {
					shippingDirectionDTO.setWriter_id(writerID);
				}
				
				map.replace("shippingDirectionDTOList", shippingDirectionDTOList);
				
				// ?????? ????????? ??????
				dailyOrderHistoryDao.insertShippingDirectionList(map);
				

				// out_pre ??????
				String pro_io_cd = "out_pre";
				
				String pro_io_memo = "?????? ???????????? ?????? ?????? ??????";
				
				String order_no_flag = "on";
				
				map.put("pro_io_cd",pro_io_cd);
				map.put("pro_io_memo",pro_io_memo);
				map.put("order_no_flag",order_no_flag);
				
				dailyOrderHistoryDao.insertProductIOWarehouse(map);
				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return shipNo;
	}

	@Override
	public List<ProductDetailDTO> getProductDetailByProNo(String pro_no) {
		List<ProductDetailVO> productDetailVOList = dailyOrderHistoryDao.selectProductDetailByProNo(pro_no);
		
		Iterator<ProductDetailVO> iteratorProductDetailVO = productDetailVOList.iterator();
		
		List<ProductDetailDTO> productDetailDTOList = new LinkedList<>();
		while(iteratorProductDetailVO.hasNext()) {
			ProductDetailVO productDetailVO = iteratorProductDetailVO.next();

			ProductDetailDTO productDetailDTO = new ProductDetailDTO(productDetailVO);
			productDetailDTOList.add(productDetailDTO);
		}
		
		
		return productDetailDTOList;
	}

	@Override
	public int createPurchaseInfoReturnPurchaseNo(List<PurchaseAndShippingInfoDTO> purchaseAndShippingInfoDTOList) {
		
		List<PurchaseInfoDTO> purchaseInfoDTOList = new ArrayList<>();
		
		Iterator<PurchaseAndShippingInfoDTO> purchaseAndShippingInfoDTOIterator = purchaseAndShippingInfoDTOList.iterator();

		Map<String, Object> map = new HashMap<>();
		
		while(purchaseAndShippingInfoDTOIterator.hasNext()) {
			PurchaseAndShippingInfoDTO purchaseAndShippingInfoDTO = purchaseAndShippingInfoDTOIterator.next();
			PurchaseInfoDTO purchaseInfoDTO = new PurchaseInfoDTO(purchaseAndShippingInfoDTO);
			purchaseInfoDTOList.add(purchaseInfoDTO);
		}
		
		// ?????? ??? purchase key??? ???????????????
		PurchaseInfoDTO firstPurchaseInfoDTO = purchaseInfoDTOList.remove(0);
		int purchaseNo = 0;
		try {
			// 1?????? ??????
			if(purchaseInfoDTOList.isEmpty()) {

				dailyOrderHistoryDao.insertPurchaseInfo(firstPurchaseInfoDTO);
				purchaseNo = firstPurchaseInfoDTO.getPurchase_no();
				
				// ?????? ????????? ??????
				dailyOrderHistoryDao.insertPurchaseDirection(firstPurchaseInfoDTO);
				
				
				purchaseInfoDTOList.add(firstPurchaseInfoDTO);
				// product_io_warehouse??? in_pre ??????
				map.put("purchaseInfoDTOList", purchaseInfoDTOList);
				
				String pro_io_cd = "in_pre";
				
				String pro_io_memo = "?????? ???????????? ?????? ?????? ??????";
				
				String purchase_no_flag = "on";
				
				map.put("pro_io_cd",pro_io_cd);
				map.put("pro_io_memo",pro_io_memo);
				map.put("purchase_no_flag",purchase_no_flag);
				
				dailyOrderHistoryDao.insertProductIOWarehouse(map);
				
			} else {
				dailyOrderHistoryDao.insertPurchaseInfo(firstPurchaseInfoDTO);
				purchaseNo = firstPurchaseInfoDTO.getPurchase_no();
				
				logger.info("??? ship_no : " + purchaseNo);
				
				for(PurchaseInfoDTO purchaseInfoDTO : purchaseInfoDTOList) {
					purchaseInfoDTO.setPurchase_no(purchaseNo);
				}
				
				map.put("purchaseInfoDTOList", purchaseInfoDTOList);
				
				dailyOrderHistoryDao.insertPurchaseInfoList(map);
				
				// ?????? ????????? ?????? ?????? ?????? ??? ?????? ??????
				purchaseInfoDTOList.add(firstPurchaseInfoDTO);

				map.replace("purchaseInfoDTOList", purchaseInfoDTOList);
				
				// ?????? ????????? ??????
				dailyOrderHistoryDao.insertPurchaseDirectionList(map);
				
				// product_io_warehouse??? in_pre ??????
				String pro_io_cd = "in_pre";
				
				String pro_io_memo = "?????? ???????????? ?????? ?????? ??????";
				
				String purchase_no_flag = "on";
				
				map.put("pro_io_cd",pro_io_cd);
				map.put("pro_io_memo",pro_io_memo);
				map.put("purchase_no_flag",purchase_no_flag);
				
				dailyOrderHistoryDao.insertProductIOWarehouse(map);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		return purchaseNo;
	}

	@Override
	public List<DailyOrderListDTO> getDailyOrderListByDateAndOrderCD(OrdersRequestDTO orderRequestDTO) {
		
		List<DailyOrderListVO> dailyOrderVOList = dailyOrderHistoryDao.selectDailyOrderListByDateAndOrderCD(orderRequestDTO);
		
		Iterator<DailyOrderListVO> dailyOrderVOListIterator = dailyOrderVOList.iterator();
		
		List<DailyOrderListDTO> dailyOrderDTOList = new LinkedList<>();
		
		while(dailyOrderVOListIterator.hasNext()) {
			DailyOrderListVO dailyOrderVO= dailyOrderVOListIterator.next();
			DailyOrderListDTO dailyOrderDTO = new DailyOrderListDTO(dailyOrderVO);
			
			dailyOrderDTOList.add(dailyOrderDTO);
		}
		
		return dailyOrderDTOList;
	}

	@Override
	public int countDailyOrderListByDateAndOrderCD(OrdersRequestDTO orderRequestDTO) {
		
		return dailyOrderHistoryDao.countListDailyOrderByDateAndOrderCD(orderRequestDTO);
	}
	
}
