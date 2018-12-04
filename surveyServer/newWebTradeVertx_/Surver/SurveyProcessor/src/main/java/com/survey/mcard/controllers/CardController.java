package com.survey.mcard.controllers;

public class CardController {
	private static CardController instance =null;
	
	public static CardController getInstance() {
		if(instance==null) {
			synchronized (CardController.class) {
				instance = new CardController();
			}
		}
		return instance;
	}
	
	
}
