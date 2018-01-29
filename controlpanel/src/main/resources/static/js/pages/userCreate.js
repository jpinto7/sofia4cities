var UserCreateController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'Sofia4Cities Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 0;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	
	// CONTROLLER PRIVATE FUNCTIONS
	

	
	// REDIRECT URL
	var navigateUrl = function(url){ window.location.href = url; }
	
		
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
	}
	
	// CHECK DATES AND LET THE FORM SUBMMIT
	var checkCreate = function(){
		logControl ? console.log('checkCreate() -> ') : '';
        
		var dateCreated = $("#datecreated").datepicker('getDate');
        var dateDeleted = $("#datedeleted").datepicker('getDate');
		
		var diff = dateDeleted - dateCreated;
		var days = diff / 1000 / 60 / 60 / 24;
				
		logControl ?  console.log('created: ' + dateCreated + '  deleted: ' + dateDeleted): '';		
		
        if (dateDeleted != ""){
            if (dateCreated > dateDeleted){
                $.confirm({icon: 'fa fa-warning', title: 'CONFIRM:', theme: 'dark',
					content: userCreateReg.validation_dates,
					draggable: true,
					dragWindowGap: 100,
					backgroundDismiss: true,
					closeIcon: true,
					buttons: {				
						close: { text: userCreateReg.Close, btnClass: 'btn btn-sm btn-default btn-outline', action: function (){} //GENERIC CLOSE.		
						}
					}
				});
                $("#datedeleted").datepicker('update','');
            }			           
        }
    } 
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
        var form1 = $('#user_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');
		
		// set current language
		currentLanguage = userCreateReg.language || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not(.selectpicker)", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
					datedeleted: { checkdates : userCreateReg.validation_dates }
			},
			// validation rules
            rules: {
				userId:		{ minlength: 5, required: true },
                fullName:	{ minlength: 5, required: true },
                email:		{ required: true, email: true },
                password:	{ required: true, minlength: 7, maxlength: 20 },
                roles:		{ required: true },
				datecreated:{ date: true, required: true },
				datedeleted:{ date: true }
            },
            invalidHandler: function(event, validator) { //display error alert on form submit              
                success1.hide();
                error1.show();
                App.scrollTo(error1, -200);
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
                success1.show();
                error1.hide();
				form.submit();
            }
        });
    }
	
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: ' + currentLanguage) : '';
		
		// selectpicker validate fix when handleValidation()
		$('.selectpicker').on('change', function () {
			$(this).valid();
		});
		
		// set current language and formats
		currentLanguage = userCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';		
		
		logControl ? console.log('|---> datepickers currentLanguage: ' + currentLanguage) : '';
		
		// init datepickers dateCreated and dateDeleted		
		$("#datecreated").datepicker({dateFormat: currentFormat, showButtonPanel: true,  orientation: "bottom auto", todayHighlight: true, todayBtn: "linked", clearBtn: true, language: currentLanguage});
        var dd = $("#datedeleted").datepicker({dateFormat: currentFormat, showButtonPanel: true,  orientation: "bottom auto", todayHighlight: true, todayBtn: "linked", clearBtn: true, language: currentLanguage});
		
		// setting on changeDate to checkDates()
		dd.on('changeDate', function(e){
				//gets the full date formated
				selectedDate = dd.data('datepicker').getFormattedDate(currentFormat);				
				checkCreate();
		});
		
		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('user_create_form');
		});
		
		//set TODAY to dateCreated depends on language
		var f = new Date();         
        today = (currentLanguage == 'es') ? ('0' + (f.getDate())).slice(-2) + "/" + ('0' + (f.getMonth()+1)).slice(-2) + "/" + f.getFullYear() : ('0' + (f.getMonth()+1)).slice(-2) + "/" + ('0' + (f.getDate())).slice(-2) + "/" + f.getFullYear();
		$('#datecreated').datepicker('update',today);
		
	}

	// CONTROLLER PUBLIC FUNCTIONS 
	return{		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return userCreateReg = Data;
		},	
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleValidation();
			initTemplateElements();		
			
		}		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	UserCreateController.load(userCreateJson);	
		
	// AUTO INIT CONTROLLER.
	UserCreateController.init();
});