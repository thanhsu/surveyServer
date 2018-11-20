package com.survey.internal.action;

import com.survey.utils.FieldName;

public class GetImage extends InternalSurveyBaseAction {

	@Override
	public void doProccess() {
		String imageID = getMessageBody().getString(FieldName.IMAGEID) ;
		
	}

}
