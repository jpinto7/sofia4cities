(function () {
  'use strict';

  angular.module('s2DashboardFramework', ['angular-gridster2', 'ngSanitize', 'ngMaterial', 'lfNgMdFileInput', 'color.picker', 'ui.codemirror', 'ngStomp', 'ngAnimate', 'angular-d3-word-cloud', 'chart.js', 'ui-leaflet', 'nemLogging', 'md.data.table', '720kb.tooltips'])
})();

(function () {
  'use strict';

  PageController.$inject = ["$log", "$scope", "$mdSidenav", "$mdDialog", "datasourceSolverService"];
  angular.module('s2DashboardFramework')
    .component('page', {
      templateUrl: 'app/components/view/pageComponent/page.html',
      controller: PageController,
      controllerAs: 'vm',
      bindings:{
        page:"=",
        editmode:"<",
        gridoptions:"<",
        dashboardheader:"<"
      }
    });

  /** @ngInject */
  function PageController($log, $scope, $mdSidenav, $mdDialog, datasourceSolverService) {
    var vm = this;
    vm.$onInit = function () {
      setTimeout(function () {
        vm.sidenav = $mdSidenav('left');
      }, 200);
    };

    vm.$postLink = function(){

    }

    vm.$onDestroy = function(){
      /*
      Not necesary
      datasourceSolverService.disconnect();
      */
    }

    function eventStop(item, itemComponent, event) {
      $log.info('eventStop', item, itemComponent, event);
    }

    function itemChange(item, itemComponent) {
      $log.info('itemChanged', item, itemComponent);
    }

    function itemResize(item, itemComponent) {
    
      $log.info('itemResized', item, itemComponent);
    }

    function itemInit(item, itemComponent) {
      $log.info('itemInitialized', item, itemComponent);
    }

    function itemRemoved(item, itemComponent) {
      $log.info('itemRemoved', item, itemComponent);
    }

    function gridInit(grid) {
      $log.info('gridInit', grid);
    }

    function gridDestroy(grid) {
      $log.info('gridDestroy', grid);
    }

    vm.prevent = function (event) {
      event.stopPropagation();
      event.preventDefault();
    };


  }
})();

(function () {
  'use strict';

  LiveHTMLController.$inject = ["$log", "$scope", "$element", "$mdCompiler", "$compile", "datasourceSolverService", "sofia2HttpService", "interactionService", "utilsService"];
  angular.module('s2DashboardFramework')
    .component('livehtml', {
      templateUrl: 'app/components/view/liveHTMLComponent/livehtml.html',
      controller: LiveHTMLController,
      controllerAs: 'vm',
      bindings:{
        id:"=?",
        livecontent:"<",
        datasource:"<",
        datastatus:"=?"
      }
    });

  /** @ngInject */
  function LiveHTMLController($log, $scope, $element, $mdCompiler, $compile, datasourceSolverService,sofia2HttpService,interactionService,utilsService) {
    var vm = this;
    $scope.ds = [];
    vm.status = "initial";

    vm.$onInit = function(){
      //register Gadget in interaction service when gadget has id
      if(vm.id){
        interactionService.registerGadget(vm.id);
      }
      //Activate incoming events
      vm.unsubscribeHandler = $scope.$on(vm.id,eventLProcessor);
      compileContent();
    }

   

    $scope.parseDSArray = function(name){
      var result=[];
      var properties=[];
      if(typeof name !="undefined" && name != null){
      try {
          for(var propertyName in $scope.ds[0]) {
            properties.push(propertyName);
          }
          if(properties.indexOf(name) > -1){
          for (var index = 0; index <  $scope.ds.length; index++) {             
              
                result.push($scope.ds[index][name]);               
              }          
            }        
          
      } catch (error) {
        
      }
    }
      return result;
    }



    vm.$onChanges = function(changes,c,d,e) {
      if("datasource" in changes && changes["datasource"].currentValue){
        refreshSubscriptionDatasource(changes.datasource.currentValue, changes.datasource.previousValue)
      }
      else{
        compileContent();
      }
    };

    $scope.getTime = function(){
      var date  = new Date();
      return date.getTime();
    }

    $scope.sendFilter = function(field, value){
      var filterStt = {};
      filterStt[field]={value: value, id: vm.id};
      interactionService.sendBroadcastFilter(vm.id,filterStt);
    }
    
    $scope.sendFilterChain = function(field, value){
      var filterStt = angular.copy(vm.datastatus)||{};
      filterStt[field]={value: value, id: vm.id};
      interactionService.sendBroadcastFilter(vm.id,filterStt);
    }

    vm.insertSofia2Http = function(token, clientPlatform, clientPlatformId, ontology, data){
      sofia2HttpService.insertSofia2Http(token, clientPlatform, clientPlatformId, ontology, data).then(
        function(e){
          console.log("OK Rest: " + JSON.stringify(e));
        }).catch(function(e){
          console.log("Fail Rest: " + JSON.stringify(e));
        });
    }

    vm.$onDestroy = function(){
      if($scope.unsubscribeHandler){
        $scope.unsubscribeHandler();
        $scope.unsubscribeHandler=null;
        datasourceSolverService.unregisterDatasourceTrigger(oldDatasource.name,oldDatasource.name);
      }
    }

    function compileContent(){
      var parentElement = $element[0];
      $mdCompiler.compile({
        template: vm.livecontent
      }).then(function (compileData) {
        compileData.link($scope);
        $element.empty();
        $element.prepend(compileData.element)
      });
    }

    function refreshSubscriptionDatasource(newDatasource, oldDatasource) {
      if($scope.unsubscribeHandler){
        $scope.unsubscribeHandler();
        $scope.unsubscribeHandler=null;
        datasourceSolverService.unregisterDatasourceTrigger(oldDatasource.name,oldDatasource.name);
      }

      datasourceSolverService.registerSingleDatasourceAndFirstShot(//Raw datasource no group, filter or projections
        {
          type: newDatasource.type,
          name: newDatasource.name,
          refresh: newDatasource.refresh,
          triggers: [{params:{filter:[],group:[],project:[]},emitTo:vm.id}]
        }
      );
    };

    function eventLProcessor(event,dataEvent){
      if(dataEvent.type === "data" && dataEvent.data.length===0){
        vm.type="nodata";
      }
      else{
        switch(dataEvent.type){
          case "data":
            switch(dataEvent.name){
              case "refresh":
                if(vm.status === "initial" || vm.status === "ready"){
                  $scope.ds = dataEvent.data;
                }
                else{
                  console.log("Ignoring refresh event, status " + vm.status);
                }
                break;
              case "add":
                $scope.ds.concat(data);
                break;
              case "filter":
                if(vm.status === "pending"){
                  $scope.ds = dataEvent.data;
                  vm.status = "ready";
                }
                break;
              default:
                console.error("Not allowed data event: " + dataEvent.name);
                break;
            } 
            break;
          case "filter":
            vm.status = "pending";
            vm.type = "loading";
            if(!vm.datastatus){
              vm.datastatus = {};
            }
            if(dataEvent.data.length){
              for(var index in dataEvent.data){
                vm.datastatus[angular.copy(dataEvent.data[index].field)] = {
                  value: angular.copy(dataEvent.data[index].value),
                  id: angular.copy(dataEvent.id)
                }
              }
            }
            else{
              delete vm.datastatus[dataEvent.field];
              if(Object.keys(vm.datastatus).length === 0 ){
                vm.datastatus = undefined;
              }
            }
            datasourceSolverService.updateDatasourceTriggerAndShot(vm.id,buildFilterStt(dataEvent));
            break;
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    function buildFilterStt(dataEvent){
      return {
        filter: {
          id: dataEvent.id,
          data: dataEvent.data.map(
            function(f){
              //quotes for string identification
              if(typeof f.value === "string"){
                f.value = "\"" + f.value + "\""
              }
              return {
                field: f.field,
                op: "=",
                exp: f.value
              }
            }
          )
        } , 
        group:[], 
        project:vm.projects
      }
    }
  }
})();

(function () {
  'use strict';

  ElementController.$inject = ["$log", "$scope", "$mdDialog", "$sanitize", "$sce", "$rootScope", "gadgetManagerService"];
  angular.module('s2DashboardFramework')
    .component('element', {
      templateUrl: 'app/components/view/elementComponent/element.html',
      controller: ElementController,
      controllerAs: 'vm',
      bindings:{
        element:"=",
        editmode:"<"
      }
    });

  /** @ngInject */
  function ElementController($log, $scope, $mdDialog, $sanitize, $sce, $rootScope, gadgetManagerService) {
    EditContainerDialog.$inject = ["$scope", "$mdDialog", "element"];
    EditGadgetDialog.$inject = ["$scope", "$mdDialog", "$http", "element", "sofia2HttpService"];
    var vm = this;
    vm.$onInit = function () {
      inicializeIncomingsEvents();
    };

    function inicializeIncomingsEvents(){
      $scope.$on("global.style",
        function(ev,style){
          angular.merge(vm.element,vm.element,style);
        }
      );

      /* Global handler by id */
      /*$scope.$on(vm.element.id,
        function(ev,data){
          angular.merge(vm.element,vm.element,data);
        }
      );*/
    }

    vm.openEditContainerDialog = function (ev) {
      $mdDialog.show({
        controller: EditContainerDialog,
        templateUrl: 'app/partials/edit/editContainerDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function EditContainerDialog($scope, $mdDialog, element) {
      $scope.icons = [
        "3d_rotation",
        "ac_unit",
        "access_alarm",
        "access_alarms",
        "access_time",
        "accessibility",
        "accessible",
        "account_balance",
        "account_balance_wallet",
        "account_box",
        "account_circle",
        "adb",
        "add",
        "add_a_photo",
        "add_alarm",
        "add_alert",
        "add_box",
        "add_circle",
        "add_circle_outline",
        "add_location",
        "add_shopping_cart",
        "add_to_photos",
        "add_to_queue",
        "adjust",
        "airline_seat_flat",
        "airline_seat_flat_angled",
        "airline_seat_individual_suite",
        "airline_seat_legroom_extra",
        "airline_seat_legroom_normal",
        "airline_seat_legroom_reduced",
        "airline_seat_recline_extra",
        "airline_seat_recline_normal",
        "airplanemode_active",
        "airplanemode_inactive",
        "airplay",
        "airport_shuttle",
        "alarm",
        "alarm_add",
        "alarm_off",
        "alarm_on",
        "album",
        "all_inclusive",
        "all_out",
        "android",
        "announcement",
        "apps",
        "archive",
        "arrow_back",
        "arrow_downward",
        "arrow_drop_down",
        "arrow_drop_down_circle",
        "arrow_drop_up",
        "arrow_forward",
        "arrow_upward",
        "art_track",
        "aspect_ratio",
        "assessment",
        "assignment",
        "assignment_ind",
        "assignment_late",
        "assignment_return",
        "assignment_returned",
        "assignment_turned_in",
        "assistant",
        "assistant_photo",
        "attach_file",
        "attach_money",
        "attachment",
        "audiotrack",
        "autorenew",
        "av_timer",
        "backspace",
        "backup",
        "battery_alert",
        "battery_charging_full",
        "battery_full",
        "battery_std",
        "battery_unknown",
        "beach_access",
        "beenhere",
        "block",
        "bluetooth",
        "bluetooth_audio",
        "bluetooth_connected",
        "bluetooth_disabled",
        "bluetooth_searching",
        "blur_circular",
        "blur_linear",
        "blur_off",
        "blur_on",
        "book",
        "bookmark",
        "bookmark_border",
        "border_all",
        "border_bottom",
        "border_clear",
        "border_color",
        "border_horizontal",
        "border_inner",
        "border_left",
        "border_outer",
        "border_right",
        "border_style",
        "border_top",
        "border_vertical",
        "branding_watermark",
        "brightness_1",
        "brightness_2",
        "brightness_3",
        "brightness_4",
        "brightness_5",
        "brightness_6",
        "brightness_7",
        "brightness_auto",
        "brightness_high",
        "brightness_low",
        "brightness_medium",
        "broken_image",
        "brush",
        "bubble_chart",
        "bug_report",
        "build",
        "burst_mode",
        "business",
        "business_center",
        "cached",
        "cake",
        "call",
        "call_end",
        "call_made",
        "call_merge",
        "call_missed",
        "call_missed_outgoing",
        "call_received",
        "call_split",
        "call_to_action",
        "camera",
        "camera_alt",
        "camera_enhance",
        "camera_front",
        "camera_rear",
        "camera_roll",
        "cancel",
        "card_giftcard",
        "card_membership",
        "card_travel",
        "casino",
        "cast",
        "cast_connected",
        "center_focus_strong",
        "center_focus_weak",
        "change_history",
        "chat",
        "chat_bubble",
        "chat_bubble_outline",
        "check",
        "check_box",
        "check_box_outline_blank",
        "check_circle",
        "chevron_left",
        "chevron_right",
        "child_care",
        "child_friendly",
        "chrome_reader_mode",
        "class",
        "clear",
        "clear_all",
        "close",
        "closed_caption",
        "cloud",
        "cloud_circle",
        "cloud_done",
        "cloud_download",
        "cloud_off",
        "cloud_queue",
        "cloud_upload",
        "code",
        "collections",
        "collections_bookmark",
        "color_lens",
        "colorize",
        "comment",
        "compare",
        "compare_arrows",
        "computer",
        "confirmation_number",
        "contact_mail",
        "contact_phone",
        "contacts",
        "content_copy",
        "content_cut",
        "content_paste",
        "control_point",
        "control_point_duplicate",
        "copyright",
        "create",
        "create_new_folder",
        "credit_card",
        "crop",
        "crop_16_9",
        "crop_3_2",
        "crop_5_4",
        "crop_7_5",
        "crop_din",
        "crop_free",
        "crop_landscape",
        "crop_original",
        "crop_portrait",
        "crop_rotate",
        "crop_square",
        "dashboard",
        "data_usage",
        "date_range",
        "dehaze",
        "delete",
        "delete_forever",
        "delete_sweep",
        "description",
        "desktop_mac",
        "desktop_windows",
        "details",
        "developer_board",
        "developer_mode",
        "device_hub",
        "devices",
        "devices_other",
        "dialer_sip",
        "dialpad",
        "directions",
        "directions_bike",
        "directions_boat",
        "directions_bus",
        "directions_car",
        "directions_railway",
        "directions_run",
        "directions_subway",
        "directions_transit",
        "directions_walk",
        "disc_full",
        "dns",
        "do_not_disturb",
        "do_not_disturb_alt",
        "do_not_disturb_off",
        "do_not_disturb_on",
        "dock",
        "domain",
        "done",
        "done_all",
        "donut_large",
        "donut_small",
        "drafts",
        "drag_handle",
        "drive_eta",
        "dvr",
        "edit",
        "edit_location",
        "eject",
        "email",
        "enhanced_encryption",
        "equalizer",
        "error",
        "error_outline",
        "euro_symbol",
        "ev_station",
        "event",
        "event_available",
        "event_busy",
        "event_note",
        "event_seat",
        "exit_to_app",
        "expand_less",
        "expand_more",
        "explicit",
        "explore",
        "exposure",
        "exposure_neg_1",
        "exposure_neg_2",
        "exposure_plus_1",
        "exposure_plus_2",
        "exposure_zero",
        "extension",
        "face",
        "fast_forward",
        "fast_rewind",
        "favorite",
        "favorite_border",
        "featured_play_list",
        "featured_video",
        "feedback",
        "fiber_dvr",
        "fiber_manual_record",
        "fiber_new",
        "fiber_pin",
        "fiber_smart_record",
        "file_download",
        "file_upload",
        "filter",
        "filter_1",
        "filter_2",
        "filter_3",
        "filter_4",
        "filter_5",
        "filter_6",
        "filter_7",
        "filter_8",
        "filter_9",
        "filter_9_plus",
        "filter_b_and_w",
        "filter_center_focus",
        "filter_drama",
        "filter_frames",
        "filter_hdr",
        "filter_list",
        "filter_none",
        "filter_tilt_shift",
        "filter_vintage",
        "find_in_page",
        "find_replace",
        "fingerprint",
        "first_page",
        "fitness_center",
        "flag",
        "flare",
        "flash_auto",
        "flash_off",
        "flash_on",
        "flight",
        "flight_land",
        "flight_takeoff",
        "flip",
        "flip_to_back",
        "flip_to_front",
        "folder",
        "folder_open",
        "folder_shared",
        "folder_special",
        "font_download",
        "format_align_center",
        "format_align_justify",
        "format_align_left",
        "format_align_right",
        "format_bold",
        "format_clear",
        "format_color_fill",
        "format_color_reset",
        "format_color_text",
        "format_indent_decrease",
        "format_indent_increase",
        "format_italic",
        "format_line_spacing",
        "format_list_bulleted",
        "format_list_numbered",
        "format_paint",
        "format_quote",
        "format_shapes",
        "format_size",
        "format_strikethrough",
        "format_textdirection_l_to_r",
        "format_textdirection_r_to_l",
        "format_underlined",
        "forum",
        "forward",
        "forward_10",
        "forward_30",
        "forward_5",
        "free_breakfast",
        "fullscreen",
        "fullscreen_exit",
        "functions",
        "g_translate",
        "gamepad",
        "games",
        "gavel",
        "gesture",
        "get_app",
        "gif",
        "golf_course",
        "gps_fixed",
        "gps_not_fixed",
        "gps_off",
        "grade",
        "gradient",
        "grain",
        "graphic_eq",
        "grid_off",
        "grid_on",
        "group",
        "group_add",
        "group_work",
        "hd",
        "hdr_off",
        "hdr_on",
        "hdr_strong",
        "hdr_weak",
        "headset",
        "headset_mic",
        "healing",
        "hearing",
        "help",
        "help_outline",
        "high_quality",
        "highlight",
        "highlight_off",
        "history",
        "home",
        "hot_tub",
        "hotel",
        "hourglass_empty",
        "hourglass_full",
        "http",
        "https",
        "image",
        "image_aspect_ratio",
        "import_contacts",
        "import_export",
        "important_devices",
        "inbox",
        "indeterminate_check_box",
        "info",
        "info_outline",
        "input",
        "insert_chart",
        "insert_comment",
        "insert_drive_file",
        "insert_emoticon",
        "insert_invitation",
        "insert_link",
        "insert_photo",
        "invert_colors",
        "invert_colors_off",
        "iso",
        "keyboard",
        "keyboard_arrow_down",
        "keyboard_arrow_left",
        "keyboard_arrow_right",
        "keyboard_arrow_up",
        "keyboard_backspace",
        "keyboard_capslock",
        "keyboard_hide",
        "keyboard_return",
        "keyboard_tab",
        "keyboard_voice",
        "kitchen",
        "label",
        "label_outline",
        "landscape",
        "language",
        "laptop",
        "laptop_chromebook",
        "laptop_mac",
        "laptop_windows",
        "last_page",
        "launch",
        "layers",
        "layers_clear",
        "leak_add",
        "leak_remove",
        "lens",
        "library_add",
        "library_books",
        "library_music",
        "lightbulb_outline",
        "line_style",
        "line_weight",
        "linear_scale",
        "link",
        "linked_camera",
        "list",
        "live_help",
        "live_tv",
        "local_activity",
        "local_airport",
        "local_atm",
        "local_bar",
        "local_cafe",
        "local_car_wash",
        "local_convenience_store",
        "local_dining",
        "local_drink",
        "local_florist",
        "local_gas_station",
        "local_grocery_store",
        "local_hospital",
        "local_hotel",
        "local_laundry_service",
        "local_library",
        "local_mall",
        "local_movies",
        "local_offer",
        "local_parking",
        "local_pharmacy",
        "local_phone",
        "local_pizza",
        "local_play",
        "local_post_office",
        "local_printshop",
        "local_see",
        "local_shipping",
        "local_taxi",
        "location_city",
        "location_disabled",
        "location_off",
        "location_on",
        "location_searching",
        "lock",
        "lock_open",
        "lock_outline",
        "looks",
        "looks_3",
        "looks_4",
        "looks_5",
        "looks_6",
        "looks_one",
        "looks_two",
        "loop",
        "loupe",
        "low_priority",
        "loyalty",
        "mail",
        "mail_outline",
        "map",
        "markunread",
        "markunread_mailbox",
        "memory",
        "menu",
        "merge_type",
        "message",
        "mic",
        "mic_none",
        "mic_off",
        "mms",
        "mode_comment",
        "mode_edit",
        "monetization_on",
        "money_off",
        "monochrome_photos",
        "mood",
        "mood_bad",
        "more",
        "more_horiz",
        "more_vert",
        "motorcycle",
        "mouse",
        "move_to_inbox",
        "movie",
        "movie_creation",
        "movie_filter",
        "multiline_chart",
        "music_note",
        "music_video",
        "my_location",
        "nature",
        "nature_people",
        "navigate_before",
        "navigate_next",
        "navigation",
        "near_me",
        "network_cell",
        "network_check",
        "network_locked",
        "network_wifi",
        "new_releases",
        "next_week",
        "nfc",
        "no_encryption",
        "no_sim",
        "not_interested",
        "note",
        "note_add",
        "notifications",
        "notifications_active",
        "notifications_none",
        "notifications_off",
        "notifications_paused",
        "offline_pin",
        "ondemand_video",
        "opacity",
        "open_in_browser",
        "open_in_new",
        "open_with",
        "pages",
        "pageview",
        "palette",
        "pan_tool",
        "panorama",
        "panorama_fish_eye",
        "panorama_horizontal",
        "panorama_vertical",
        "panorama_wide_angle",
        "party_mode",
        "pause",
        "pause_circle_filled",
        "pause_circle_outline",
        "payment",
        "people",
        "people_outline",
        "perm_camera_mic",
        "perm_contact_calendar",
        "perm_data_setting",
        "perm_device_information",
        "perm_identity",
        "perm_media",
        "perm_phone_msg",
        "perm_scan_wifi",
        "person",
        "person_add",
        "person_outline",
        "person_pin",
        "person_pin_circle",
        "personal_video",
        "pets",
        "phone",
        "phone_android",
        "phone_bluetooth_speaker",
        "phone_forwarded",
        "phone_in_talk",
        "phone_iphone",
        "phone_locked",
        "phone_missed",
        "phone_paused",
        "phonelink",
        "phonelink_erase",
        "phonelink_lock",
        "phonelink_off",
        "phonelink_ring",
        "phonelink_setup",
        "photo",
        "photo_album",
        "photo_camera",
        "photo_filter",
        "photo_library",
        "photo_size_select_actual",
        "photo_size_select_large",
        "photo_size_select_small",
        "picture_as_pdf",
        "picture_in_picture",
        "picture_in_picture_alt",
        "pie_chart",
        "pie_chart_outlined",
        "pin_drop",
        "place",
        "play_arrow",
        "play_circle_filled",
        "play_circle_outline",
        "play_for_work",
        "playlist_add",
        "playlist_add_check",
        "playlist_play",
        "plus_one",
        "poll",
        "polymer",
        "pool",
        "portable_wifi_off",
        "portrait",
        "power",
        "power_input",
        "power_settings_new",
        "pregnant_woman",
        "present_to_all",
        "print",
        "priority_high",
        "public",
        "publish",
        "query_builder",
        "question_answer",
        "queue",
        "queue_music",
        "queue_play_next",
        "radio",
        "radio_button_checked",
        "radio_button_unchecked",
        "rate_review",
        "receipt",
        "recent_actors",
        "record_voice_over",
        "redeem",
        "redo",
        "refresh",
        "remove",
        "remove_circle",
        "remove_circle_outline",
        "remove_from_queue",
        "remove_red_eye",
        "remove_shopping_cart",
        "reorder",
        "repeat",
        "repeat_one",
        "replay",
        "replay_10",
        "replay_30",
        "replay_5",
        "reply",
        "reply_all",
        "report",
        "report_problem",
        "restaurant",
        "restaurant_menu",
        "restore",
        "restore_page",
        "ring_volume",
        "room",
        "room_service",
        "rotate_90_degrees_ccw",
        "rotate_left",
        "rotate_right",
        "rounded_corner",
        "router",
        "rowing",
        "rss_feed",
        "rv_hookup",
        "satellite",
        "save",
        "scanner",
        "schedule",
        "school",
        "screen_lock_landscape",
        "screen_lock_portrait",
        "screen_lock_rotation",
        "screen_rotation",
        "screen_share",
        "sd_card",
        "sd_storage",
        "search",
        "security",
        "select_all",
        "send",
        "sentiment_dissatisfied",
        "sentiment_neutral",
        "sentiment_satisfied",
        "sentiment_very_dissatisfied",
        "sentiment_very_satisfied",
        "settings",
        "settings_applications",
        "settings_backup_restore",
        "settings_bluetooth",
        "settings_brightness",
        "settings_cell",
        "settings_ethernet",
        "settings_input_antenna",
        "settings_input_component",
        "settings_input_composite",
        "settings_input_hdmi",
        "settings_input_svideo",
        "settings_overscan",
        "settings_phone",
        "settings_power",
        "settings_remote",
        "settings_system_daydream",
        "settings_voice",
        "share",
        "shop",
        "shop_two",
        "shopping_basket",
        "shopping_cart",
        "short_text",
        "show_chart",
        "shuffle",
        "signal_cellular_4_bar",
        "signal_cellular_connected_no_internet_4_bar",
        "signal_cellular_no_sim",
        "signal_cellular_null",
        "signal_cellular_off",
        "signal_wifi_4_bar",
        "signal_wifi_4_bar_lock",
        "signal_wifi_off",
        "sim_card",
        "sim_card_alert",
        "skip_next",
        "skip_previous",
        "slideshow",
        "slow_motion_video",
        "smartphone",
        "smoke_free",
        "smoking_rooms",
        "sms",
        "sms_failed",
        "snooze",
        "sort",
        "sort_by_alpha",
        "spa",
        "space_bar",
        "speaker",
        "speaker_group",
        "speaker_notes",
        "speaker_notes_off",
        "speaker_phone",
        "spellcheck",
        "star",
        "star_border",
        "star_half",
        "stars",
        "stay_current_landscape",
        "stay_current_portrait",
        "stay_primary_landscape",
        "stay_primary_portrait",
        "stop",
        "stop_screen_share",
        "storage",
        "store",
        "store_mall_directory",
        "straighten",
        "streetview",
        "strikethrough_s",
        "style",
        "subdirectory_arrow_left",
        "subdirectory_arrow_right",
        "subject",
        "subscriptions",
        "subtitles",
        "subway",
        "supervisor_account",
        "surround_sound",
        "swap_calls",
        "swap_horiz",
        "swap_vert",
        "swap_vertical_circle",
        "switch_camera",
        "switch_video",
        "sync",
        "sync_disabled",
        "sync_problem",
        "system_update",
        "system_update_alt",
        "tab",
        "tab_unselected",
        "tablet",
        "tablet_android",
        "tablet_mac",
        "tag_faces",
        "tap_and_play",
        "terrain",
        "text_fields",
        "text_format",
        "textsms",
        "texture",
        "theaters",
        "thumb_down",
        "thumb_up",
        "thumbs_up_down",
        "time_to_leave",
        "timelapse",
        "timeline",
        "timer",
        "timer_10",
        "timer_3",
        "timer_off",
        "title",
        "toc",
        "today",
        "toll",
        "tonality",
        "touch_app",
        "toys",
        "track_changes",
        "traffic",
        "train",
        "tram",
        "transfer_within_a_station",
        "transform",
        "translate",
        "trending_down",
        "trending_flat",
        "trending_up",
        "tune",
        "turned_in",
        "turned_in_not",
        "tv",
        "unarchive",
        "undo",
        "unfold_less",
        "unfold_more",
        "update",
        "usb",
        "verified_user",
        "vertical_align_bottom",
        "vertical_align_center",
        "vertical_align_top",
        "vibration",
        "video_call",
        "video_label",
        "video_library",
        "videocam",
        "videocam_off",
        "videogame_asset",
        "view_agenda",
        "view_array",
        "view_carousel",
        "view_column",
        "view_comfy",
        "view_compact",
        "view_day",
        "view_headline",
        "view_list",
        "view_module",
        "view_quilt",
        "view_stream",
        "view_week",
        "vignette",
        "visibility",
        "visibility_off",
        "voice_chat",
        "voicemail",
        "volume_down",
        "volume_mute",
        "volume_off",
        "volume_up",
        "vpn_key",
        "vpn_lock",
        "wallpaper",
        "warning",
        "watch",
        "watch_later",
        "wb_auto",
        "wb_cloudy",
        "wb_incandescent",
        "wb_iridescent",
        "wb_sunny",
        "wc",
        "web",
        "web_asset",
        "weekend",
        "whatshot",
        "widgets",
        "wifi",
        "wifi_lock",
        "wifi_tethering",
        "work",
        "wrap_text",
        "youtube_searched_for",
        "zoom_in",
        "zoom_out",
        "zoom_out_map"
      ];

      $scope.element = element;

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };
    }

    vm.openEditGadgetDialog = function (ev) {
      $mdDialog.show({
        controller: EditGadgetDialog,
        templateUrl: 'app/partials/edit/editGadgetDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        multiple : true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        locals: {
          element: vm.element
        }
      })
      .then(function(answer) {
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    function EditGadgetDialog($scope, $mdDialog, $http, element, sofia2HttpService) {


      $scope.element = element;

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.answer = function(answer) {
        $mdDialog.hide(answer);
      };

      $scope.datasources = [];

      $scope.loadDatasources = function(){
        return sofia2HttpService.getDatasources().then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      };

    }

    vm.trustHTML = function(html_code) {
      return $sce.trustAsHtml(html_code)
    }

    vm.calcHeight = function(){
      vm.element.header.height = (vm.element.header.height=='inherit'?25:vm.element.header.height);
      return "calc(100% - " + (vm.element.header.enable?vm.element.header.height:0) + "px)";
    }

    vm.deleteElement = function(){
      $rootScope.$broadcast("deleteElement",vm.element);
    }

    vm.generateFilterInfo = function(filter){
      return filter.value + ' (' + gadgetManagerService.findGadgetById(filter.id).header.title.text + ')';
    }

    vm.deleteFilter = function(id, field){
      $rootScope.$broadcast(vm.element.id,{id: id,type:'filter',data:[],field:field})
    }
  }
})();

(function () {
  'use strict';

  GadgetController.$inject = ["$log", "$scope", "$element", "$window", "$mdCompiler", "$compile", "datasourceSolverService", "sofia2HttpService", "interactionService", "utilsService", "leafletMarkerEvents", "leafletData"];
  angular.module('s2DashboardFramework')
    .component('gadget', {
      templateUrl: 'app/components/view/gadgetComponent/gadget.html',
      controller: GadgetController,
      controllerAs: 'vm',
      bindings:{
        id:"<?",
        gconfig:"=?",
        gmeasures:"=?",
        gdatasourceid:"=?",
        datastatus:"=?"
      }
    });

  /** @ngInject */
  function GadgetController($log, $scope, $element, $window, $mdCompiler, $compile, datasourceSolverService, sofia2HttpService, interactionService, utilsService, leafletMarkerEvents, leafletData) {
    var vm = this;
    vm.ds = [];
    vm.type = "loading";
    vm.config = {};//Gadget database config
    vm.measures = [];
    vm.status = "initial";
    vm.selected = [];
    vm.notSmall=true;
 
    //Chaining filters, used to propagate own filters to child elements
    vm.filterChaining=true;

    vm.$onInit = function(){
      //register Gadget in interaction service when gadget has id
      if(vm.id){
        interactionService.registerGadget(vm.id);
      }
      //Activate incoming events
      vm.unsubscribeHandler = $scope.$on(vm.id,eventGProcessor);
      $scope.reloadContent();
    }

    $scope.reloadContent = function(){
      /*Gadget Editor Mode*/
      if(!vm.id){
        //vm.config = vm.gconfig;//gadget config
        if(!vm.config.config){
          return;//Init editor triggered
        }
        if(typeof vm.config.config == "string"){
          vm.config.config = JSON.parse(vm.config.config);
        }
        //vm.measures = vm.gmeasures;//gadget config
        var projects = [];
        for(var index=0; index < vm.measures.length; index++){
          var jsonConfig = JSON.parse(vm.measures[index].config);
          for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
            if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },projects)){
              projects.push({op:"",field:jsonConfig.fields[indexF]});
            }
          }
          //add attribute for filter style marker to recover from datasource.
          if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            projects.push({op:"",field:vm.config.config.markersFilter});
          }
          vm.measures[index].config = jsonConfig;
        }
        sofia2HttpService.getDatasourceById(vm.ds).then(
          function(datasource){
            subscriptionDatasource(datasource.data, [], projects, []);
          }
        )
      }
      else{
      /*View Mode*/
        sofia2HttpService.getGadgetConfigById(
          vm.id
        ).then(
          function(config){
            vm.config=config.data;
            vm.config.config = JSON.parse(vm.config.config);
            return sofia2HttpService.getGadgetMeasuresByGadgetId(vm.id);
          }
        ).then(
          function(measures){
            vm.measures = measures.data;

            vm.projects = [];
            for(var index=0; index < vm.measures.length; index++){
              var jsonConfig = JSON.parse(vm.measures[index].config);
              for(var indexF = 0 ; indexF < jsonConfig.fields.length; indexF++){
                if(!utilsService.isSameJsonInArray( { op:"", field:jsonConfig.fields[indexF] },vm.projects)){
                  vm.projects.push({op:"",field:jsonConfig.fields[indexF]});
                }
              }
               //add attribute for filter style marker to recover from datasource.
             if(vm.config.type=="map" && typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
              vm.projects.push({op:"",field:vm.config.config.markersFilter});
             }
              vm.measures[index].config = jsonConfig;
            }
            sofia2HttpService.getDatasourceById(vm.measures[0].datasource.id).then(
              function(datasource){
                subscriptionDatasource(datasource.data, [], vm.projects, []);
              }
            )
          }
        )
      }
    }

    vm.$onChanges = function(changes) {

    };

    vm.$onDestroy = function(){
      if(vm.unsubscribeHandler){
        vm.unsubscribeHandler();
        vm.unsubscribeHandler=null;
        datasourceSolverService.unregisterDatasourceTrigger(vm.measures[0].datasource);
      }
    }

    function subscriptionDatasource(datasource, filter, project, group) {

      datasourceSolverService.registerSingleDatasourceAndFirstShot(//Raw datasource no group, filter or projections
        {
          type: datasource.mode,
          name: datasource.identification,
          refresh: datasource.refresh,
          triggers: [{params:{filter:filter, group:group, project:project},emitTo:vm.id}]
        }
      );
    };

    function processDataToGadget(data){ //With dynamic loading this will change
      switch(vm.config.type){
        case "line":
        case "bar":
        case "radar":
        case "pie":
          //Group X axis values
          var allLabelsField = [];
          for(var index=0; index < vm.measures.length; index++){
            allLabelsField = allLabelsField.concat(data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)}));
          }
          allLabelsField = utilsService.sort_unique(allLabelsField);

          //Match Y values
          var allDataField = [];//Data values sort by labels
          for(var index=0; index < vm.measures.length; index++){
            var dataRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[1],ind)});
            var labelRawSerie = data.map(function(d,ind){return utilsService.getJsonValueByJsonPath(d,vm.measures[index].config.fields[0],ind)});
            var sortedArray = [];
            for(var indexf = 0; indexf < dataRawSerie.length; indexf++){
              sortedArray[allLabelsField.indexOf(labelRawSerie[indexf])] = dataRawSerie[indexf];
            }
            allDataField.push(sortedArray);
          }

          vm.labels = allLabelsField;
          vm.series = vm.measures.map (function(m){return m.config.name});

          if(vm.config.type == "pie"){
            vm.data = allDataField[0];
          }
          else{
            vm.data = allDataField;
          }

          var baseOptionsChart = {
            legend: {
              display: true, 
              labels: {
                boxWidth: 11
              }
            }, 
            maintainAspectRatio: false, 
            responsive: true, 
            responsiveAnimationDuration:500
          };

          vm.datasetOverride = vm.measures.map (function(m){return m.config.config});

          vm.optionsChart = angular.merge({},vm.config.config,baseOptionsChart);

          break;
        case 'wordcloud':
          //Get data in an array
          var arrayWordSplited = data.reduce(function(a,b){return a.concat(b.value.split(" "))},[])//data.flatMap(function(d){return getJsonValueByJsonPath(d,vm.measures[index].config.fields[0]).split(" ")})
          var hashWords = {};
          var counterArray = []
          for(var index = 0; index < arrayWordSplited.length; index++){
            var word = arrayWordSplited[index];
            if(word in hashWords){
              counterArray[hashWords[word]].count++;
            }
            else{
              hashWords[word]=counterArray.length;
              counterArray.push({text:word,count:1});
            }
          }

          vm.counterArray = counterArray.sort(function(a, b){
            return b.count - a.count;
          })
          redrawWordCloud();
          $scope.$on("$resize",redrawWordCloud);
          break;
        case "map":
          leafletData.getDirectiveControls('lmap' + vm.id).then(function (controls) {
            if(controls.markers){
              controls.markers.clean();
            }
          });

          vm.center = vm.center || vm.config.config.center;
          //IF defined intervals for marker 
          if(typeof vm.config.config.jsonMarkers!=undefined && vm.config.config.jsonMarkers!=null && vm.config.config.jsonMarkers.length>0){
            var jsonMarkers = JSON.parse(vm.config.config.jsonMarkers);
            
            vm.markers = data.map(
              function(d){
                return {
                  lat: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[0],0),
                  lng: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[1],1),
  
                  message: vm.measures[0].config.fields.slice(3).reduce(
                    function(a, b){
                      return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                    }
                    ,""
                  ),
                  id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2),
                  icon: utilsService.getMarkerForMap(utilsService.getJsonValueByJsonPath(d,vm.config.config.markersFilter,2),jsonMarkers),
                }
              }
            )
          
          }else{
          vm.markers = data.map(
            function(d){
              return {
                lat: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[0],0),
                lng: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[1],1),

                message: vm.measures[0].config.fields.slice(3).reduce(
                  function(a, b){
                    return a + "<b>" + b + ":</b>&nbsp;" + utilsService.getJsonValueByJsonPath(d,b) + "<br/>";
                  }
                  ,""
                ),
                id: utilsService.getJsonValueByJsonPath(d,vm.measures[0].config.fields[2],2)
               
              }
            }
          )
        }

          $scope.events = {
            markers: {
                enable: leafletMarkerEvents.getAvailableEvents(),
            }
          };
          
          //Init map events
          var eventName = 'leafletDirectiveMarker.lmap' + vm.id + '.click';
          $scope.$on(eventName, vm.clickMarkerMapEventProcessorEmitter);

          redrawLeafletMap();
          $scope.$on("$resize",redrawLeafletMap);
          break;
          case "table":
          vm.data=data;
          if(data.length>0){
            var i = 0;
            for(var propertyName in data[0]) {
              vm.measures[i].config.order = propertyName;
              i++;
           }
          }          
          vm.config.config.tablePagination.limitOptions = vm.config.config.tablePagination.options.limitSelect ? [5, 10, 20, 50 ,100]  : undefined;
          redrawTable();
          $scope.$on("$resize",redrawTable);
          break;
      }

      vm.type = vm.config.type;//Activate gadget
      utilsService.forceRender($scope);
    }

    function redrawWordCloud(){
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      var maxCount = vm.counterArray[0].count;
      var minCount = vm.counterArray[vm.counterArray.length - 1].count;
      var maxWordSize = width * 0.15;
      var minWordSize = maxWordSize / 5;
      var spread = maxCount - minCount;
      if (spread <= 0) spread = 1;
      var step = (maxWordSize - minWordSize) / spread;
      vm.words = vm.counterArray.map(function(word) {
          return {
              text: word.text,
              size: Math.round(maxWordSize - ((maxCount - word.count) * step)),
              tooltipText: word.count + ' ocurrences'
          }
      })
      vm.width = width;
      vm.height = height;
    }

    function redrawTable(){
     var element = $element[0];   
      var width = element.offsetWidth;
      
      if(width<600){
        vm.notSmall=false;
      }else{
        vm.notSmall=true;
      }
    }


    function redrawLeafletMap(){
      var element = $element[0];
      var height = element.offsetHeight;
      var width = element.offsetWidth;
      vm.width = width;
      vm.height = height;
    }

    function eventGProcessor(event,dataEvent){
      if(dataEvent.type === "data" && dataEvent.data.length===0){
        vm.type="nodata";
        vm.status = "ready";
      }
      else{
        switch(dataEvent.type){
          case "data":
            switch(dataEvent.name){
              case "refresh":
                if(vm.status === "initial" || vm.status === "ready"){
                  processDataToGadget(dataEvent.data);
                }
                else{
                  console.log("Ignoring refresh event, status " + vm.status);
                }
                break;
              case "add":
                //processDataToGadget(data);
                break;
              case "filter":
                if(vm.status === "pending"){
                  processDataToGadget(dataEvent.data);
                  vm.status = "ready";
                }
                break;
              case "drillup":
                //processDataToGadget(data);
                break;
              case "drilldown":
                //processDataToGadget(data);
                break;
              default:
                console.error("Not allowed data event: " + dataEvent.name);
                break;
            } 
            break;
          case "filter":
            vm.status = "pending";
            //vm.type = "loading";
            if(!vm.datastatus){
              vm.datastatus = {};
            }
            if(dataEvent.data.length){
              for(var index in dataEvent.data){
                vm.datastatus[angular.copy(dataEvent.data[index].field)] = {
                  value: angular.copy(dataEvent.data[index].value),
                  id: angular.copy(dataEvent.id)
                }
              }
            }
            else{
              delete vm.datastatus[dataEvent.field];
              if(Object.keys(vm.datastatus).length === 0 ){
                vm.datastatus = undefined;
              }
            }
            datasourceSolverService.updateDatasourceTriggerAndShot(vm.id,buildFilterStt(dataEvent));
            break;
          default:
            console.error("Not allowed event: " + dataEvent.type);
            break;
        }
      }
      utilsService.forceRender($scope);
    }

    function buildFilterStt(dataEvent){
      return {
        filter: {
          id: dataEvent.id,
          data: dataEvent.data.map(
            function(f){
              //quotes for string identification
              if(typeof f.value === "string"){
                f.value = "\"" + f.value + "\""
              }
              return {
                field: f.field,
                op: "=",
                exp: f.value
              }
            }
          )
        } , 
        group:[], 
        project:vm.projects
      }
    }

    //Chartjs click event
    vm.clickChartEventProcessorEmitter = function(points, evt){
      var originField;
      var originValue;
      switch(vm.config.type){
        case "line":
          //originField = vm.measures[0].config.fields[0];
          //originValue = points[0]._model.label;
          break;
        case "bar":
          //find serie x field if there are diferent x field in measures
          for(var index in vm.data){
            if(vm.data[index][points[0]._index]){
              originField = vm.measures[index].config.fields[0];
              break;
            }
          }
          originValue = points[0]._model.label;
          break;
          case "radar":
          //find serie x field if there are diferent x field in measures
          for(var index in vm.data){
            if(vm.data[index][points[0]._index]){
              originField = vm.measures[index].config.fields[0];
              break;
            }
          }
          originValue = points[0]._chart.config.data.labels[points[0]._index];
          break;
        case "pie":
          originField = vm.measures[0].config.fields[0];
          originValue = points[0]._model.label;
          break;
      }
      sendEmitterEvent(originField,originValue);
    }

    //leafletjs click marker event, by Point Id
    vm.clickMarkerMapEventProcessorEmitter = function(event, args){
      var originField = vm.measures[0].config.fields[2];
      var originValue = args.model.id;
      sendEmitterEvent(originField,originValue);
    }

    vm.selectItemTable = function (item) {
      console.log(item, 'was selected');
      for (var index = 0; index < vm.measures.length; index++) {
        var element = vm.measures[index];
        var originField = element.config.fields[0];
        var originValue = item[element.config.order];
        sendEmitterEvent(originField,originValue);
      }      
    };
  

    function sendEmitterEvent(originField,originValue){
      if(vm.filterChaining){
        var filterStt = angular.copy(vm.datastatus)||{}
      }
      else{
        var filterStt = {}
      }
      filterStt[originField]={value: originValue, id: vm.id};
      interactionService.sendBroadcastFilter(vm.id,filterStt);
    }
  }
})();

(function () {
  'use strict';

  EditDashboardController.$inject = ["$log", "$window", "__env", "$scope", "$mdSidenav", "$mdDialog", "$mdBottomSheet", "sofia2HttpService", "interactionService"];
  angular.module('s2DashboardFramework')
    .component('editDashboard', {
      templateUrl: 'app/components/edit/editDashboardComponent/edit.dashboard.html',
      controller: EditDashboardController,
      controllerAs: 'ed',
      bindings: {
        "dashboard":"=",
        "public":"&",
        "id":"&",
        "selectedpage" : "&"
      }
    });

  /** @ngInject */
  function EditDashboardController($log, $window,__env, $scope, $mdSidenav, $mdDialog, $mdBottomSheet, sofia2HttpService, interactionService) {
    PagesController.$inject = ["$scope", "$mdDialog", "dashboard", "icons", "$timeout"];
    LayersController.$inject = ["$scope", "$mdDialog", "dashboard", "selectedpage", "selectedlayer"];
    DatasourcesController.$inject = ["$scope", "$mdDialog", "$http", "dashboard", "selectedpage"];
    EditDashboardController.$inject = ["$scope", "$mdDialog", "dashboard", "$timeout"];
    EditDashboardStyleController.$inject = ["$scope", "$rootScope", "$mdDialog", "style", "$timeout"];
    DatalinkController.$inject = ["$scope", "$rootScope", "$mdDialog", "interactionService", "utilsService", "sofia2HttpService", "dashboard", "selectedpage"];
    AddWidgetBottomSheetController.$inject = ["$scope", "$mdBottomSheet"];
    var ed = this;

    //Gadget source connection type list
    var typeGadgetList = ["pie","bar","map","livehtml","radar","table"];

    ed.$onInit = function () {
      ed.selectedlayer = 0;
      //ed.selectedpage = ed.selectedpage;
      ed.icons = [
        "3d_rotation",
        "ac_unit",
        "access_alarm",
        "access_alarms",
        "access_time",
        "accessibility",
        "accessible",
        "account_balance",
        "account_balance_wallet",
        "account_box",
        "account_circle",
        "adb",
        "add",
        "add_a_photo",
        "add_alarm",
        "add_alert",
        "add_box",
        "add_circle",
        "add_circle_outline",
        "add_location",
        "add_shopping_cart",
        "add_to_photos",
        "add_to_queue",
        "adjust",
        "airline_seat_flat",
        "airline_seat_flat_angled",
        "airline_seat_individual_suite",
        "airline_seat_legroom_extra",
        "airline_seat_legroom_normal",
        "airline_seat_legroom_reduced",
        "airline_seat_recline_extra",
        "airline_seat_recline_normal",
        "airplanemode_active",
        "airplanemode_inactive",
        "airplay",
        "airport_shuttle",
        "alarm",
        "alarm_add",
        "alarm_off",
        "alarm_on",
        "album",
        "all_inclusive",
        "all_out",
        "android",
        "announcement",
        "apps",
        "archive",
        "arrow_back",
        "arrow_downward",
        "arrow_drop_down",
        "arrow_drop_down_circle",
        "arrow_drop_up",
        "arrow_forward",
        "arrow_upward",
        "art_track",
        "aspect_ratio",
        "assessment",
        "assignment",
        "assignment_ind",
        "assignment_late",
        "assignment_return",
        "assignment_returned",
        "assignment_turned_in",
        "assistant",
        "assistant_photo",
        "attach_file",
        "attach_money",
        "attachment",
        "audiotrack",
        "autorenew",
        "av_timer",
        "backspace",
        "backup",
        "battery_alert",
        "battery_charging_full",
        "battery_full",
        "battery_std",
        "battery_unknown",
        "beach_access",
        "beenhere",
        "block",
        "bluetooth",
        "bluetooth_audio",
        "bluetooth_connected",
        "bluetooth_disabled",
        "bluetooth_searching",
        "blur_circular",
        "blur_linear",
        "blur_off",
        "blur_on",
        "book",
        "bookmark",
        "bookmark_border",
        "border_all",
        "border_bottom",
        "border_clear",
        "border_color",
        "border_horizontal",
        "border_inner",
        "border_left",
        "border_outer",
        "border_right",
        "border_style",
        "border_top",
        "border_vertical",
        "branding_watermark",
        "brightness_1",
        "brightness_2",
        "brightness_3",
        "brightness_4",
        "brightness_5",
        "brightness_6",
        "brightness_7",
        "brightness_auto",
        "brightness_high",
        "brightness_low",
        "brightness_medium",
        "broken_image",
        "brush",
        "bubble_chart",
        "bug_report",
        "build",
        "burst_mode",
        "business",
        "business_center",
        "cached",
        "cake",
        "call",
        "call_end",
        "call_made",
        "call_merge",
        "call_missed",
        "call_missed_outgoing",
        "call_received",
        "call_split",
        "call_to_action",
        "camera",
        "camera_alt",
        "camera_enhance",
        "camera_front",
        "camera_rear",
        "camera_roll",
        "cancel",
        "card_giftcard",
        "card_membership",
        "card_travel",
        "casino",
        "cast",
        "cast_connected",
        "center_focus_strong",
        "center_focus_weak",
        "change_history",
        "chat",
        "chat_bubble",
        "chat_bubble_outline",
        "check",
        "check_box",
        "check_box_outline_blank",
        "check_circle",
        "chevron_left",
        "chevron_right",
        "child_care",
        "child_friendly",
        "chrome_reader_mode",
        "class",
        "clear",
        "clear_all",
        "close",
        "closed_caption",
        "cloud",
        "cloud_circle",
        "cloud_done",
        "cloud_download",
        "cloud_off",
        "cloud_queue",
        "cloud_upload",
        "code",
        "collections",
        "collections_bookmark",
        "color_lens",
        "colorize",
        "comment",
        "compare",
        "compare_arrows",
        "computer",
        "confirmation_number",
        "contact_mail",
        "contact_phone",
        "contacts",
        "content_copy",
        "content_cut",
        "content_paste",
        "control_point",
        "control_point_duplicate",
        "copyright",
        "create",
        "create_new_folder",
        "credit_card",
        "crop",
        "crop_16_9",
        "crop_3_2",
        "crop_5_4",
        "crop_7_5",
        "crop_din",
        "crop_free",
        "crop_landscape",
        "crop_original",
        "crop_portrait",
        "crop_rotate",
        "crop_square",
        "dashboard",
        "data_usage",
        "date_range",
        "dehaze",
        "delete",
        "delete_forever",
        "delete_sweep",
        "description",
        "desktop_mac",
        "desktop_windows",
        "details",
        "developer_board",
        "developer_mode",
        "device_hub",
        "devices",
        "devices_other",
        "dialer_sip",
        "dialpad",
        "directions",
        "directions_bike",
        "directions_boat",
        "directions_bus",
        "directions_car",
        "directions_railway",
        "directions_run",
        "directions_subway",
        "directions_transit",
        "directions_walk",
        "disc_full",
        "dns",
        "do_not_disturb",
        "do_not_disturb_alt",
        "do_not_disturb_off",
        "do_not_disturb_on",
        "dock",
        "domain",
        "done",
        "done_all",
        "donut_large",
        "donut_small",
        "drafts",
        "drag_handle",
        "drive_eta",
        "dvr",
        "edit",
        "edit_location",
        "eject",
        "email",
        "enhanced_encryption",
        "equalizer",
        "error",
        "error_outline",
        "euro_symbol",
        "ev_station",
        "event",
        "event_available",
        "event_busy",
        "event_note",
        "event_seat",
        "exit_to_app",
        "expand_less",
        "expand_more",
        "explicit",
        "explore",
        "exposure",
        "exposure_neg_1",
        "exposure_neg_2",
        "exposure_plus_1",
        "exposure_plus_2",
        "exposure_zero",
        "extension",
        "face",
        "fast_forward",
        "fast_rewind",
        "favorite",
        "favorite_border",
        "featured_play_list",
        "featured_video",
        "feedback",
        "fiber_dvr",
        "fiber_manual_record",
        "fiber_new",
        "fiber_pin",
        "fiber_smart_record",
        "file_download",
        "file_upload",
        "filter",
        "filter_1",
        "filter_2",
        "filter_3",
        "filter_4",
        "filter_5",
        "filter_6",
        "filter_7",
        "filter_8",
        "filter_9",
        "filter_9_plus",
        "filter_b_and_w",
        "filter_center_focus",
        "filter_drama",
        "filter_frames",
        "filter_hdr",
        "filter_list",
        "filter_none",
        "filter_tilt_shift",
        "filter_vintage",
        "find_in_page",
        "find_replace",
        "fingerprint",
        "first_page",
        "fitness_center",
        "flag",
        "flare",
        "flash_auto",
        "flash_off",
        "flash_on",
        "flight",
        "flight_land",
        "flight_takeoff",
        "flip",
        "flip_to_back",
        "flip_to_front",
        "folder",
        "folder_open",
        "folder_shared",
        "folder_special",
        "font_download",
        "format_align_center",
        "format_align_justify",
        "format_align_left",
        "format_align_right",
        "format_bold",
        "format_clear",
        "format_color_fill",
        "format_color_reset",
        "format_color_text",
        "format_indent_decrease",
        "format_indent_increase",
        "format_italic",
        "format_line_spacing",
        "format_list_bulleted",
        "format_list_numbered",
        "format_paint",
        "format_quote",
        "format_shapes",
        "format_size",
        "format_strikethrough",
        "format_textdirection_l_to_r",
        "format_textdirection_r_to_l",
        "format_underlined",
        "forum",
        "forward",
        "forward_10",
        "forward_30",
        "forward_5",
        "free_breakfast",
        "fullscreen",
        "fullscreen_exit",
        "functions",
        "g_translate",
        "gamepad",
        "games",
        "gavel",
        "gesture",
        "get_app",
        "gif",
        "golf_course",
        "gps_fixed",
        "gps_not_fixed",
        "gps_off",
        "grade",
        "gradient",
        "grain",
        "graphic_eq",
        "grid_off",
        "grid_on",
        "group",
        "group_add",
        "group_work",
        "hd",
        "hdr_off",
        "hdr_on",
        "hdr_strong",
        "hdr_weak",
        "headset",
        "headset_mic",
        "healing",
        "hearing",
        "help",
        "help_outline",
        "high_quality",
        "highlight",
        "highlight_off",
        "history",
        "home",
        "hot_tub",
        "hotel",
        "hourglass_empty",
        "hourglass_full",
        "http",
        "https",
        "image",
        "image_aspect_ratio",
        "import_contacts",
        "import_export",
        "important_devices",
        "inbox",
        "indeterminate_check_box",
        "info",
        "info_outline",
        "input",
        "insert_chart",
        "insert_comment",
        "insert_drive_file",
        "insert_emoticon",
        "insert_invitation",
        "insert_link",
        "insert_photo",
        "invert_colors",
        "invert_colors_off",
        "iso",
        "keyboard",
        "keyboard_arrow_down",
        "keyboard_arrow_left",
        "keyboard_arrow_right",
        "keyboard_arrow_up",
        "keyboard_backspace",
        "keyboard_capslock",
        "keyboard_hide",
        "keyboard_return",
        "keyboard_tab",
        "keyboard_voice",
        "kitchen",
        "label",
        "label_outline",
        "landscape",
        "language",
        "laptop",
        "laptop_chromebook",
        "laptop_mac",
        "laptop_windows",
        "last_page",
        "launch",
        "layers",
        "layers_clear",
        "leak_add",
        "leak_remove",
        "lens",
        "library_add",
        "library_books",
        "library_music",
        "lightbulb_outline",
        "line_style",
        "line_weight",
        "linear_scale",
        "link",
        "linked_camera",
        "list",
        "live_help",
        "live_tv",
        "local_activity",
        "local_airport",
        "local_atm",
        "local_bar",
        "local_cafe",
        "local_car_wash",
        "local_convenience_store",
        "local_dining",
        "local_drink",
        "local_florist",
        "local_gas_station",
        "local_grocery_store",
        "local_hospital",
        "local_hotel",
        "local_laundry_service",
        "local_library",
        "local_mall",
        "local_movies",
        "local_offer",
        "local_parking",
        "local_pharmacy",
        "local_phone",
        "local_pizza",
        "local_play",
        "local_post_office",
        "local_printshop",
        "local_see",
        "local_shipping",
        "local_taxi",
        "location_city",
        "location_disabled",
        "location_off",
        "location_on",
        "location_searching",
        "lock",
        "lock_open",
        "lock_outline",
        "looks",
        "looks_3",
        "looks_4",
        "looks_5",
        "looks_6",
        "looks_one",
        "looks_two",
        "loop",
        "loupe",
        "low_priority",
        "loyalty",
        "mail",
        "mail_outline",
        "map",
        "markunread",
        "markunread_mailbox",
        "memory",
        "menu",
        "merge_type",
        "message",
        "mic",
        "mic_none",
        "mic_off",
        "mms",
        "mode_comment",
        "mode_edit",
        "monetization_on",
        "money_off",
        "monochrome_photos",
        "mood",
        "mood_bad",
        "more",
        "more_horiz",
        "more_vert",
        "motorcycle",
        "mouse",
        "move_to_inbox",
        "movie",
        "movie_creation",
        "movie_filter",
        "multiline_chart",
        "music_note",
        "music_video",
        "my_location",
        "nature",
        "nature_people",
        "navigate_before",
        "navigate_next",
        "navigation",
        "near_me",
        "network_cell",
        "network_check",
        "network_locked",
        "network_wifi",
        "new_releases",
        "next_week",
        "nfc",
        "no_encryption",
        "no_sim",
        "not_interested",
        "note",
        "note_add",
        "notifications",
        "notifications_active",
        "notifications_none",
        "notifications_off",
        "notifications_paused",
        "offline_pin",
        "ondemand_video",
        "opacity",
        "open_in_browser",
        "open_in_new",
        "open_with",
        "pages",
        "pageview",
        "palette",
        "pan_tool",
        "panorama",
        "panorama_fish_eye",
        "panorama_horizontal",
        "panorama_vertical",
        "panorama_wide_angle",
        "party_mode",
        "pause",
        "pause_circle_filled",
        "pause_circle_outline",
        "payment",
        "people",
        "people_outline",
        "perm_camera_mic",
        "perm_contact_calendar",
        "perm_data_setting",
        "perm_device_information",
        "perm_identity",
        "perm_media",
        "perm_phone_msg",
        "perm_scan_wifi",
        "person",
        "person_add",
        "person_outline",
        "person_pin",
        "person_pin_circle",
        "personal_video",
        "pets",
        "phone",
        "phone_android",
        "phone_bluetooth_speaker",
        "phone_forwarded",
        "phone_in_talk",
        "phone_iphone",
        "phone_locked",
        "phone_missed",
        "phone_paused",
        "phonelink",
        "phonelink_erase",
        "phonelink_lock",
        "phonelink_off",
        "phonelink_ring",
        "phonelink_setup",
        "photo",
        "photo_album",
        "photo_camera",
        "photo_filter",
        "photo_library",
        "photo_size_select_actual",
        "photo_size_select_large",
        "photo_size_select_small",
        "picture_as_pdf",
        "picture_in_picture",
        "picture_in_picture_alt",
        "pie_chart",
        "pie_chart_outlined",
        "pin_drop",
        "place",
        "play_arrow",
        "play_circle_filled",
        "play_circle_outline",
        "play_for_work",
        "playlist_add",
        "playlist_add_check",
        "playlist_play",
        "plus_one",
        "poll",
        "polymer",
        "pool",
        "portable_wifi_off",
        "portrait",
        "power",
        "power_input",
        "power_settings_new",
        "pregnant_woman",
        "present_to_all",
        "print",
        "priority_high",
        "public",
        "publish",
        "query_builder",
        "question_answer",
        "queue",
        "queue_music",
        "queue_play_next",
        "radio",
        "radio_button_checked",
        "radio_button_unchecked",
        "rate_review",
        "receipt",
        "recent_actors",
        "record_voice_over",
        "redeem",
        "redo",
        "refresh",
        "remove",
        "remove_circle",
        "remove_circle_outline",
        "remove_from_queue",
        "remove_red_eye",
        "remove_shopping_cart",
        "reorder",
        "repeat",
        "repeat_one",
        "replay",
        "replay_10",
        "replay_30",
        "replay_5",
        "reply",
        "reply_all",
        "report",
        "report_problem",
        "restaurant",
        "restaurant_menu",
        "restore",
        "restore_page",
        "ring_volume",
        "room",
        "room_service",
        "rotate_90_degrees_ccw",
        "rotate_left",
        "rotate_right",
        "rounded_corner",
        "router",
        "rowing",
        "rss_feed",
        "rv_hookup",
        "satellite",
        "save",
        "scanner",
        "schedule",
        "school",
        "screen_lock_landscape",
        "screen_lock_portrait",
        "screen_lock_rotation",
        "screen_rotation",
        "screen_share",
        "sd_card",
        "sd_storage",
        "search",
        "security",
        "select_all",
        "send",
        "sentiment_dissatisfied",
        "sentiment_neutral",
        "sentiment_satisfied",
        "sentiment_very_dissatisfied",
        "sentiment_very_satisfied",
        "settings",
        "settings_applications",
        "settings_backup_restore",
        "settings_bluetooth",
        "settings_brightness",
        "settings_cell",
        "settings_ethernet",
        "settings_input_antenna",
        "settings_input_component",
        "settings_input_composite",
        "settings_input_hdmi",
        "settings_input_svideo",
        "settings_overscan",
        "settings_phone",
        "settings_power",
        "settings_remote",
        "settings_system_daydream",
        "settings_voice",
        "share",
        "shop",
        "shop_two",
        "shopping_basket",
        "shopping_cart",
        "short_text",
        "show_chart",
        "shuffle",
        "signal_cellular_4_bar",
        "signal_cellular_connected_no_internet_4_bar",
        "signal_cellular_no_sim",
        "signal_cellular_null",
        "signal_cellular_off",
        "signal_wifi_4_bar",
        "signal_wifi_4_bar_lock",
        "signal_wifi_off",
        "sim_card",
        "sim_card_alert",
        "skip_next",
        "skip_previous",
        "slideshow",
        "slow_motion_video",
        "smartphone",
        "smoke_free",
        "smoking_rooms",
        "sms",
        "sms_failed",
        "snooze",
        "sort",
        "sort_by_alpha",
        "spa",
        "space_bar",
        "speaker",
        "speaker_group",
        "speaker_notes",
        "speaker_notes_off",
        "speaker_phone",
        "spellcheck",
        "star",
        "star_border",
        "star_half",
        "stars",
        "stay_current_landscape",
        "stay_current_portrait",
        "stay_primary_landscape",
        "stay_primary_portrait",
        "stop",
        "stop_screen_share",
        "storage",
        "store",
        "store_mall_directory",
        "straighten",
        "streetview",
        "strikethrough_s",
        "style",
        "subdirectory_arrow_left",
        "subdirectory_arrow_right",
        "subject",
        "subscriptions",
        "subtitles",
        "subway",
        "supervisor_account",
        "surround_sound",
        "swap_calls",
        "swap_horiz",
        "swap_vert",
        "swap_vertical_circle",
        "switch_camera",
        "switch_video",
        "sync",
        "sync_disabled",
        "sync_problem",
        "system_update",
        "system_update_alt",
        "tab",
        "tab_unselected",
        "tablet",
        "tablet_android",
        "tablet_mac",
        "tag_faces",
        "tap_and_play",
        "terrain",
        "text_fields",
        "text_format",
        "textsms",
        "texture",
        "theaters",
        "thumb_down",
        "thumb_up",
        "thumbs_up_down",
        "time_to_leave",
        "timelapse",
        "timeline",
        "timer",
        "timer_10",
        "timer_3",
        "timer_off",
        "title",
        "toc",
        "today",
        "toll",
        "tonality",
        "touch_app",
        "toys",
        "track_changes",
        "traffic",
        "train",
        "tram",
        "transfer_within_a_station",
        "transform",
        "translate",
        "trending_down",
        "trending_flat",
        "trending_up",
        "tune",
        "turned_in",
        "turned_in_not",
        "tv",
        "unarchive",
        "undo",
        "unfold_less",
        "unfold_more",
        "update",
        "usb",
        "verified_user",
        "vertical_align_bottom",
        "vertical_align_center",
        "vertical_align_top",
        "vibration",
        "video_call",
        "video_label",
        "video_library",
        "videocam",
        "videocam_off",
        "videogame_asset",
        "view_agenda",
        "view_array",
        "view_carousel",
        "view_column",
        "view_comfy",
        "view_compact",
        "view_day",
        "view_headline",
        "view_list",
        "view_module",
        "view_quilt",
        "view_stream",
        "view_week",
        "vignette",
        "visibility",
        "visibility_off",
        "voice_chat",
        "voicemail",
        "volume_down",
        "volume_mute",
        "volume_off",
        "volume_up",
        "vpn_key",
        "vpn_lock",
        "wallpaper",
        "warning",
        "watch",
        "watch_later",
        "wb_auto",
        "wb_cloudy",
        "wb_incandescent",
        "wb_iridescent",
        "wb_sunny",
        "wc",
        "web",
        "web_asset",
        "weekend",
        "whatshot",
        "widgets",
        "wifi",
        "wifi_lock",
        "wifi_tethering",
        "work",
        "wrap_text",
        "youtube_searched_for",
        "zoom_in",
        "zoom_out",
        "zoom_out_map"
      ]
      /*When global style change, that changes are broadcasted to all elements*/
      ed.global = {
        style: {
          header:{
            height: 25,
            enable: "initial",
            backgroundColor: "initial",
            title: {
              textColor: "initial",
              iconColor: "initial"
            }
          },
          border: {},
          backgroundColor: "initial"
        }
      };
    }

    ed.removePage = function (event, index) {
      event.stopPropagation();
      event.preventDefault();
      vm.dashboard.pages.splice(index, 1);
      $scope.$applyAsync();
    };

    ed.pagesEdit = function (ev) {
      $mdDialog.show({
        controller: PagesController,
        templateUrl: 'app/partials/edit/pagesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          icons: ed.icons
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog pages closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.layersEdit = function (ev) {
      $mdDialog.show({
        controller: LayersController,
        templateUrl: 'app/partials/edit/layersDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage(),
          selectedlayer: ed.selectedlayer
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog layers closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.datasourcesEdit = function (ev) {
      $mdDialog.show({
        controller: DatasourcesController,
        templateUrl: 'app/partials/edit/datasourcesDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datasources closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardController,
        templateUrl: 'app/partials/edit/editDashboardDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.dashboardStyleEdit = function (ev) {
      $mdDialog.show({
        controller: EditDashboardStyleController,
        templateUrl: 'app/partials/edit/editDashboardStyleDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          style: ed.global.style
        }
      })
      .then(function(page) {
        $scope.status = 'Dashboard Edit closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.showDatalink = function (ev) {
      $mdDialog.show({
        controller: DatalinkController,
        templateUrl: 'app/partials/edit/datalinkDialog.html',
        parent: angular.element(document.body),
        targetEvent: ev,
        clickOutsideToClose:true,
        fullscreen: false, // Only for -xs, -sm breakpoints.
        openFrom: '.toolbarButtons',
        closeTo: '.toolbarButtons',
        locals: {
          dashboard: ed.dashboard,
          selectedpage: ed.selectedpage()
        }
      })
      .then(function(page) {
        $scope.status = 'Dialog datalink closed'
      }, function() {
        $scope.status = 'You cancelled the dialog.';
      });
    };

    ed.savePage = function (ev) {
      ed.dashboard.interactionHash = interactionService.getInteractionHash();
      sofia2HttpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
        function(d){
          if(d){
            $mdDialog.show(
              $mdDialog.alert()
                .parent(angular.element(document.querySelector('document')))
                .clickOutsideToClose(true)
                .title('Dashboard Editor')
                .textContent('Your dashboard was successfully saved!')
                .ariaLabel('Save dialog')
                .ok('OK')
                .targetEvent(ev)
            )
          }
        }
      ).catch(
        function(d){
          if(d){
            console.log("Error: " + JSON.stringify(d))
            $mdDialog.show(
              $mdDialog.alert()
                .parent(angular.element(document.querySelector('document')))
                .clickOutsideToClose(true)
                .title('Dashboard Editor: ERROR')
                .textContent('There was an error saving your dashboard!')
                .ariaLabel('Save dialog')
                .ok('OK')
                .targetEvent(ev)
            )
          }
        }
      );
      //alert(JSON.stringify(ed.dashboard));
    };


    ed.deleteDashboard = function (ev) {

      var confirm = $mdDialog.confirm()
      .title('Would you like to delete Dashboard?')
      .textContent('If you delete the dashboard it will be permanent')
      .ariaLabel('Delete dialog')
      .targetEvent(ev)
      .ok('Delete')
      .cancel('Cancel');

      $mdDialog.show(confirm).then(function() {
          sofia2HttpService.deleteDashboard(ed.id()).then(
            function(d){
              if(d){
                $mdDialog.show(
                  $mdDialog.alert()
                    .parent(angular.element(document.querySelector('document')))
                    .clickOutsideToClose(true)
                    .title('Dashboard Editor')
                    .textContent('Your dashboard was successfully Deleted!')
                    .ariaLabel('Delete dialog')
                    .ok('OK')
                    .targetEvent(ev)
                ).finally(function(){
                  $window.location.href=__env.endpointSofia2ControlPanel+'/dashboards/list';
                })
              }
            }
          ).catch(
            function(d){
              if(d){
                console.log("Error: " + JSON.stringify(d))
                $mdDialog.show(
                  $mdDialog.alert()
                    .parent(angular.element(document.querySelector('document')))
                    .clickOutsideToClose(true)
                    .title('Dashboard Editor: ERROR')
                    .textContent('There was an error deleting your dashboard!')
                    .ariaLabel('Delete dialog')
                    .ok('OK')
                    .targetEvent(ev)
                )
              }
            }
          );
        
        
      }, function() {
      
      });

    }

    
    ed.closeDashboard = function (ev) {

      var confirm = $mdDialog.confirm()
      .title('Would you like to save Dashboard before Close?')
      .textContent('If you close without saving the changes will be lost')
      .ariaLabel('Close dialog')
      .targetEvent(ev)
      .ok('Save')
      .cancel('Exit');

      $mdDialog.show(confirm).then(function() {
        ed.dashboard.interactionHash = interactionService.getInteractionHash();
        sofia2HttpService.saveDashboard(ed.id(), {"data":{"model":JSON.stringify(ed.dashboard),"id":"","identification":"a","customcss":"","customjs":"","jsoni18n":"","description":"a","public":ed.public}}).then(
          function(d){
            if(d){
              $mdDialog.show(
                $mdDialog.alert()
                  .parent(angular.element(document.querySelector('document')))
                  .clickOutsideToClose(true)
                  .title('Dashboard Editor')
                  .textContent('Your dashboard was successfully saved!')
                  .ariaLabel('Save dialog')
                  .ok('OK')
                  .targetEvent(ev)
              ).finally(function(){
                $window.location.href=__env.endpointSofia2ControlPanel+'/dashboards/list';
              })
            }
          }
        ).catch(
          function(d){
            if(d){
              console.log("Error: " + JSON.stringify(d))
              $mdDialog.show(
                $mdDialog.alert()
                  .parent(angular.element(document.querySelector('document')))
                  .clickOutsideToClose(true)
                  .title('Dashboard Editor: ERROR')
                  .textContent('There was an error saving your dashboard!')
                  .ariaLabel('Save dialog')
                  .ok('OK')
                  .targetEvent(ev)
              )
            }
          }
        );
        
        
      }, function() {
        $window.location.href=__env.endpointSofia2ControlPanel+'/dashboards/list';
      });

    }


    ed.changedOptions = function changedOptions() {
      //main.options.api.optionsChanged();
    };

    function PagesController($scope, $mdDialog, dashboard, icons, $timeout) {
      $scope.dashboard = dashboard;
      $scope.icons = icons;
      $scope.auxUpload = [];
      $scope.apiUpload = [];

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          for(var page = 0; page < $scope.dashboard.pages.length; page ++){
            if($scope.dashboard.pages[page].background.file.length > 0){
              $scope.apiUpload[page].addRemoteFile($scope.dashboard.pages[page].background.file[0].filedata,$scope.dashboard.pages[page].background.file[0].lfFileName,$scope.dashboard.pages[page].background.file[0].lfTagType);
            }
          }
        });
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newPage = {};
        var newLayer = {};
        //newLayer.options = JSON.parse(JSON.stringify(ed.dashboard.pages[0].layers[0].options));
        newLayer.gridboard = [
        ];
        newLayer.title = "baseLayer";
        newPage.title = angular.copy($scope.title);
        newPage.icon = angular.copy($scope.selectedIconItem);
        newPage.layers = [newLayer];
        newPage.background = {}
        newPage.background.file = angular.copy($scope.file);
        newPage.background.color="hsl(0, 0%, 100%)";
        newPage.selectedlayer= 0;
        dashboard.pages.push(newPage);
        $scope.title = "";
        $scope.icon = "";
        $scope.file = [];
        auxReloadUploads();
        $scope.$applyAsync();
      };

      $scope.onFilesChange = function(index){
        dashboard.pages[index].background.file = $scope.auxUpload[index].file;
        if(dashboard.pages[index].background.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            dashboard.pages[index].background.filedata = e.target.result;
          };
          FR.readAsDataURL( dashboard.pages[index].background.file[0].lfFile );
        }
        else{
          dashboard.pages[index].background.filedata="";
        }
      }

      $scope.delete = function(index){
        dashboard.pages.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index-1];
        dashboard.pages[index-1] = temp;
      }

      $scope.moveDownPage = function(index){
        var temp = dashboard.pages[index];
        dashboard.pages[index] = dashboard.pages[index+1];
        dashboard.pages[index+1] = temp;
      }
    }

    function LayersController($scope, $mdDialog, dashboard, selectedpage, selectedlayer) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.selectedlayer = selectedlayer;
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var newLayer = {}
        newLayer.gridboard = [
          {cols: 5, rows: 5, y: 0, x: 0, type:"mixed"},
          {cols: 2, rows: 2, y: 0, x: 2, hasContent: true},
          {cols: 1, rows: 1, y: 0, x: 4, type:"polar"},
          {cols: 1, rows: 1, y: 2, x: 5, type:"map"},
          {cols: 2, rows: 2, y: 1, x: 0}
        ];
        newLayer.title = angular.copy($scope.title);
        dashboard.pages[$scope.selectedpage].layers.push(newLayer);
        $scope.selectedlayer = dashboard.pages[$scope.selectedpage].layers.length-1;
        $scope.title = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(index){
        dashboard.pages[$scope.selectedpage].layers.splice(index, 1);
      }

      $scope.queryIcon = function (query) {
        return query ? $scope.icons.filter( createFilterFor(query) ) : $scope.icons;
      }

      /**
       * Create filter function for a query string
       */
      function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(icon) {
          return (icon.indexOf(lowercaseQuery) != -1);
        };
      }

      $scope.moveUpLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index-1];
        dashboard.pages[$scope.selectedpage].layers[index-1] = temp;
      }

      $scope.moveDownLayer = function(index){
        var temp = dashboard.pages[$scope.selectedpage].layers[index];
        dashboard.pages[$scope.selectedpage].layers[index] = dashboard.pages[$scope.selectedpage].layers[index+1];
        dashboard.pages[$scope.selectedpage].layers[index+1] = temp;
      }
    }

    function DatasourcesController($scope, $mdDialog, $http, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;
      $scope.datasources = [];
      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.create = function() {
        var datasource = angular.copy($scope.datasource);
        dashboard.pages[$scope.selectedpage].datasources[datasource.identification]={triggers:[],type:datasource.type,interval:datasources.refresh};
        $scope.name = "";
        $scope.$applyAsync();
      };

      $scope.delete = function(key){
        delete dashboard.pages[$scope.selectedpage].datasources[key];
      }

      $scope.loadDatasources = function(){
        return $http.get('/controlpanel/datasources/getUserGadgetDatasources').then(
          function(response){
            $scope.datasources=response.data;
          },
          function(e){
            console.log("Error getting datasources: " +  JSON.stringify(e))
          }
        );
      }
    }

    function EditDashboardController($scope, $mdDialog, dashboard, $timeout) {
      $scope.dashboard = dashboard;

      function auxReloadUploads(){
        /*Load previous images*/
        $timeout(function(){
          if($scope.dashboard.header.logo.file && $scope.dashboard.header.logo.file.length > 0){
            $scope.apiUpload.addRemoteFile($scope.dashboard.header.logo.filedata,$scope.dashboard.header.logo.file[0].lfFileName,$scope.dashboard.header.logo.file[0].lfTagType);
          }
        });
      }

      $scope.onFilesChange = function(){
        $scope.dashboard.header.logo.file = $scope.auxUpload.file;
        if($scope.dashboard.header.logo.file.length > 0){
          var FR = new FileReader();
          FR.onload = function(e) {
            $scope.dashboard.header.logo.filedata = e.target.result;
          };
          FR.readAsDataURL( $scope.dashboard.header.logo.file[0].lfFile );
        }
        else{
          $scope.dashboard.header.logo.filedata="";
        }
      }

      auxReloadUploads();

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

      $scope.changedOptions = function changedOptions() {
        $scope.dashboard.gridOptions.api.optionsChanged();
      };

    }

    function compareJSON(obj1, obj2) {
      var result = {};
      for(var key in obj1) {
          if(obj2[key] != obj1[key]) result[key] = obj2[key];
          if(typeof obj2[key] == 'array' && typeof obj1[key] == 'array')
              result[key] = compareJSON(obj1[key], obj2[key]);
          if(typeof obj2[key] == 'object' && typeof obj1[key] == 'object')
              result[key] = compareJSON(obj1[key], obj2[key]);
      }
      return result;
    }

    function EditDashboardStyleController($scope,$rootScope, $mdDialog, style, $timeout) {
      $scope.style = style;

      $scope.$watch('style',function(newValue, oldValue){
        if (newValue===oldValue) {
          return;
        }
        var diffs = compareJSON(oldValue, newValue);
        $rootScope.$broadcast('global.style', diffs);
      }, true)

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }

    function DatalinkController($scope,$rootScope, $mdDialog, interactionService, utilsService, sofia2HttpService, dashboard, selectedpage) {
      $scope.dashboard = dashboard;
      $scope.selectedpage = selectedpage;

      var rawInteractions = interactionService.getInteractionHash();

      initConnectionsList();
      generateGadgetsLists();

      function initConnectionsList(){
        $scope.connections = [];
        for(var source in rawInteractions){
          for(var indexFieldTargets in rawInteractions[source]){
            for(var indexTargets in rawInteractions[source][indexFieldTargets].targetList){
              var rowInteraction = {
                source:source,
                sourceField:rawInteractions[source][indexFieldTargets].emiterField,
                target:rawInteractions[source][indexFieldTargets].targetList[indexTargets].gadgetId,
                targetField:rawInteractions[source][indexFieldTargets].targetList[indexTargets].overwriteField
              }
              $scope.connections.push(rowInteraction);
            }
          }
        }
      }

      $scope.refreshGadgetEmitterFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetEmitterFields(gadget);
        }
      }

      $scope.refreshGadgetTargetFields = function(gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          $scope.gadgetEmitterFields = [];
        }
        else{
          setGadgetTargetFields(gadget);
        }
      }

      function setGadgetEmitterFields(gadget){
        switch(gadget.type){
          case "pie":
          case "bar":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "radar":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
          case "map":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[2]})).map(function(m){return {field:m}});
            break;
          case "livehtml":
            var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
            $scope.emitterDatasource = gadgetData.datasource.name;
            sofia2HttpService.getFieldsFromDatasourceId(gadgetData.datasource.id).then(
              function(data){
                $scope.gadgetEmitterFields = utilsService.getJsonFields(data.data[0],"", []);
              }
            )
            $scope.gadgetEmitterFields = [];
            break;
            case "table":
            var gadgetMeasures = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm.measures;
            $scope.emitterDatasource = gadgetMeasures[0].datasource.identification;
            $scope.gadgetEmitterFields = utilsService.sort_unique(gadgetMeasures.map(function(m){return m.config.fields[0]})).map(function(m){return {field:m}});
            break;
        }
      }

      //Destination are all gadget fields
      function setGadgetTargetFields(gadget){
        $scope.targetDatasource="";
        var gadgetData = angular.element(document.getElementsByClassName(gadget.id)[0]).scope().$$childHead.vm;
        if(gadget.type === 'livehtml'){
          $scope.targetDatasource = gadgetData.datasource.name;
          var dsId = gadgetData.datasource.id;
        }
        else{
          $scope.targetDatasource = gadgetData.measures[0].datasource.identification;
          var dsId = gadgetData.measures[0].datasource.id;
        }
        sofia2HttpService.getFieldsFromDatasourceId(dsId).then(
          function(data){
            $scope.gadgetTargetFields = utilsService.getJsonFields(data.data[0],"", []);
          }
        )
        $scope.gadgetTargetFields = [];
      }

      //Get gadget JSON and return string info for UI
      $scope.prettyGadgetInfo = function(gadget){
       
          return gadget.header.title.text + " (" + gadget.type + ")";
        
      }

      $scope.generateGadgetInfo = function (gadgetId){
        var gadget = findGadgetInDashboard(gadgetId);
        if(gadget == null){
          return gadgetId;
        }
        else{
          return $scope.prettyGadgetInfo(gadget)
        }
      }

      function generateGadgetsLists(){
        $scope.gadgetsSources = getGadgetsSourcesInDashboard();
        $scope.gadgetsTargets = getGadgetsInDashboard();
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsSourcesInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeGadgetList.indexOf(gadget.type) != -1});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      //Generate gadget list of posible Sources of interactions: pie, bar, livehtml
      function getGadgetsInDashboard(){
        var gadgets = [];
        var page = $scope.dashboard.pages[$scope.selectedpage];
        for (var i = 0; i < page.layers.length; i++) {
          var layer = page.layers[i];
          var gadgetsAux = layer.gridboard.filter(function(gadget){return typeof gadget.id != "undefined"});
          if(gadgetsAux.length){
            gadgets = gadgets.concat(gadgetsAux);
          }
        }
        return gadgets;
      }

      function findGadgetInDashboard(gadgetId){
        for(var p=0;p<$scope.dashboard.pages.length;p++){
          var page = $scope.dashboard.pages[p];       
          for (var i = 0; i < page.layers.length; i++) {
            var layer = page.layers[i];
            var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
            if(gadgets.length){
              return gadgets[0];
            }
          }
        }
        return null;
      }

      $scope.create = function(sourceGadgetId, originField , targetGadgetId, destinationField) {
        if(sourceGadgetId && originField && targetGadgetId && destinationField){
          interactionService.registerGadgetInteractionDestination(sourceGadgetId, targetGadgetId, originField, destinationField);
          initConnectionsList();
        }
      };

      $scope.delete = function(sourceGadgetId, targetGadgetId, originField, destinationField){
        interactionService.unregisterGadgetInteractionDestination(sourceGadgetId, originField, targetGadgetId, destinationField);
        initConnectionsList();
      }

      $scope.hide = function() {
        $mdDialog.hide();
      };

      $scope.cancel = function() {
        $mdDialog.cancel();
      };

    }

    ed.showListBottomSheet = function() {
      $window.dispatchEvent(new Event("resize"));
      $mdBottomSheet.show({
        templateUrl: 'app/partials/edit/addWidgetBottomSheet.html',
        controller: AddWidgetBottomSheetController,
        disableParentScroll: false,
        disableBackdrop: true,
        clickOutsideToClose: true
      }).then(function(clickedItem) {
        $scope.alert = clickedItem['name'] + ' clicked!';
      }).catch(function(error) {
        // User clicked outside or hit escape
      });
    };

    function AddWidgetBottomSheetController($scope, $mdBottomSheet){

    }

    $scope.$on('deleteElement',function (event, item) {
      var dashboard = $scope.ed.dashboard;
      var page = dashboard.pages[$scope.ed.selectedpage()];
      var layer = page.layers[page.selectedlayer];
      layer.gridboard.splice(layer.gridboard.indexOf(item), 1);
      $scope.$applyAsync();
    });
  }
})();

(function () {
  'use strict';

  UtilsService.$inject = ["$http", "$log", "__env", "$rootScope"];
  angular.module('s2DashboardFramework')
    .service('utilsService', UtilsService);

  /** @ngInject */
  function UtilsService($http, $log, __env, $rootScope) {
      var vm = this;

      //force angular render in order to fast refresh view of component. $scope is pass as argument for render only this element
      vm.forceRender = function($scope){
        if(!$scope.$$phase) {
          $scope.$applyAsync();
        }
      }

      //Access json by string dot path
      function multiIndex(obj,is,pos) {  // obj,['1','2','3'] -> ((obj['1'])['2'])['3']
        if(is.length && !(is[0] in obj)){
          return obj[is[is.length-1]];
        }
        return is.length ? multiIndex(obj[is[0]],is.slice(1),pos) : obj
      }

      vm.getJsonValueByJsonPath = function(obj,is,pos) {
        //special case for array access, return key is 0, 1
        var matchArray = is.match(/\[[0-9]\]*$/);
        if(matchArray){
          //Get de match in is [0] and get return field name
          return obj[pos];
        }
        return multiIndex(obj,is.split('.'))
      }

      //array transform to sorted and unique values
      vm.sort_unique = function(arr) {
        if (arr.length === 0) return arr;
        var sortFn;
        if(typeof arr[0] === "string"){//String sort
          sortFn = function (a, b) {
            if(a < b) return -1;
            if(a > b) return 1;
            return 0;
          }
        }
        else{//Number and date sort
          sortFn = function (a, b) {
            return a*1 - b*1;
          }
        }
        arr = arr.sort(sortFn);
        var ret = [arr[0]];
        for (var i = 1; i < arr.length; i++) { //Start loop at 1: arr[0] can never be a duplicate
          if (arr[i-1] !== arr[i]) {
            ret.push(arr[i]);
          }
        }
        return ret;
      }

      vm.isSameJsonInArray = function(json,arrayJson){
        for(var index = 0; index < arrayJson.length; index ++){
          var equals = true;
          for(var key in arrayJson[index]){
            if(arrayJson[index][key] != json[key]){
              equals = false;
              break;
            }
          }
          if(equals){
            return true;
          }
        }
        return false;
      }

      vm.getJsonFields = function iterate(obj, stack, fields) {
        for (var property in obj) {
          if (obj.hasOwnProperty(property)) {
            if (typeof obj[property] == "object") {
              vm.getJsonFields(obj[property], stack + (stack==""?'':'.') + property, fields);
            } else {
              fields.push({field:stack + (stack==""?'':'.') + property, type:typeof obj[property]});
            }
          }
        }    
        return fields;
      }
     
      vm.getMarkerForMap = function(value,jsonMarkers) {
       
        var result = {
          type: 'vectorMarker',
          icon: 'circle',
          markerColor: 'blue',
          iconColor:"white"
        }
        var found = false;
        for (var index = 0; index < jsonMarkers.length && !found; index++) {
          var limit = jsonMarkers[index];
          var minUndefined = typeof limit.min=="undefined";
          var maxUndefined = typeof limit.max=="undefined";    
          
          if(!minUndefined && !maxUndefined){
            if(value<=limit.max && value>=limit.min){
              result.icon=limit.icon;
              result.markerColor=limit.markerColor;
              result.iconColor=limit.iconColor;
              found=true;
            }
          }else if (!minUndefined && maxUndefined){
            if( value>=limit.min){
              result.icon=limit.icon;
              result.markerColor=limit.markerColor;
              result.iconColor=limit.iconColor;
              found=true;
            }

          }else if (minUndefined && !maxUndefined){
            if( value<=limit.max){
              result.icon=limit.icon;
              result.markerColor=limit.markerColor;
              result.iconColor=limit.iconColor;
              found=true;
            }

          }
          
        }

        return result;
      }
     

     

  };
})();

(function () {
  'use strict';

  Sofia2HttpService.$inject = ["$http", "$log", "__env", "$rootScope"];
  angular.module('s2DashboardFramework')
    .service('sofia2HttpService', Sofia2HttpService);

  /** @ngInject */
  function Sofia2HttpService($http, $log, __env, $rootScope) {
      var vm = this;

      vm.getDatasources = function(){
        return $http.get(__env.endpointSofia2ControlPanel + '/datasources/getUserGadgetDatasources');
      }

      vm.getsampleDatasources = function(ds){
        return $http.get(__env.endpointSofia2ControlPanel + '/datasources/getSampleDatasource/'+ds);
      }

      vm.getDatasourceById = function(datasourceId){
        return $http.get(__env.endpointSofia2ControlPanel + '/datasources/getDatasourceById/' + datasourceId);
      }

      vm.getFieldsFromDatasourceId = function(datasourceId){
        return $http.get(__env.endpointSofia2ControlPanel + '/datasources/getSampleDatasource/' + datasourceId);
      }

      vm.getGadgetConfigById = function(gadgetId){
        return $http.get(__env.endpointSofia2ControlPanel + '/gadgets/getGadgetConfigById/' + gadgetId);
      }

      vm.getUserGadgetsByType = function(type){
        return $http.get(__env.endpointSofia2ControlPanel + '/gadgets/getUserGadgetsByType/' + type);
      }

      vm.getUserGadgetTemplate = function(){
        return $http.get(__env.endpointSofia2ControlPanel + '/gadgettemplates/getUserGadgetTemplate/');
      }

      vm.getGadgetMeasuresByGadgetId = function(gadgetId){
        return $http.get(__env.endpointSofia2ControlPanel + '/gadgets/getGadgetMeasuresByGadgetId/' + gadgetId);
      }

      vm.saveDashboard = function(id, dashboard){
        return $http.put(__env.endpointSofia2ControlPanel + '/dashboards/savemodel/' + id, {"model":dashboard.data.model,"visible":dashboard.public});
      }
      vm.deleteDashboard = function(id){
        return $http.put(__env.endpointSofia2ControlPanel + '/dashboards/delete/' + id,{});
      }

      vm.setDashboardEngineCredentialsAndLogin = function () {
        var authdata = btoa(__env.dashboardEngineUsername + ':' + __env.dashboardEnginePassword);

        $rootScope.globals = {
            currentUser: {
                username: __env.dashboardEngineUsername,
                authdata: __env.dashboardEnginePassword
            }
        };

        $http.defaults.headers.common['Authorization'] = 'Basic ' + authdata;
        return $http.get(__env.endpointSofia2DashboardEngine + __env.dashboardEngineLoginRest, {timeout : __env.dashboardEngineLoginRestTimeout});
      };

      vm.getDashboardModel = function(id){
        return $http.get(__env.endpointSofia2ControlPanel + '/dashboards/model/' + id);
      }

      vm.insertSofia2Http = function(token, clientPlatform, clientPlatformId, ontology, data){
        return $http.get(__env.sofia2RestUrl + "/client/join?token=" + token + "&clientPlatform=" + clientPlatform + "&clientPlatformId=" + clientPlatformId).then(
          function(e){
            $http.defaults.headers.common['Authorization'] = e.data.sessionKey;
            return $http.post(__env.sofia2RestUrl + "/ontology/" + ontology,data);
          }
        )
      }
  };
})();

(function () {
  'use strict';

  SocketService.$inject = ["$stomp", "$log", "__env", "$timeout"];
  angular.module('s2DashboardFramework')
    .service('socketService', SocketService);

  /** @ngInject */
  function SocketService($stomp, $log, __env, $timeout) {
      var vm = this;

      vm.stompClient = {};
      vm.hashRequestResponse = {};
      vm.connected = false;
      vm.initQueue = [];
      $stomp.setDebug(function (args) {
        $log.debug(args)
      });

      $stomp.setDebug(false);

      vm.connect = function(){
        $stomp.connect(__env.socketEndpointConnect, []).then(
          function(frame){
            if(frame.command == "CONNECTED"){
              vm.connected=true;
              while(vm.initQueue.length>0){
                var datasource = vm.initQueue.pop()
                vm.sendAndSubscribe(datasource);
              }
            }
            else{
              console.log("Error, reconnecting...")
              $timeout(vm.connect,2000);
            }
          }
        )
      }

      vm.connectAndSendAndSubscribe = function(reqrespList){
        $stomp
          .connect(__env.socketEndpointConnect, [])

          // frame = CONNECTED headers
          .then(function (frame) {
            for(var reqrest in reqrespList){
              var UUID = generateConnectionUUID();
              vm.hashRequestResponse[UUID] = reqrespList[reqrest];
              vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
                var answerId = headers.destination.split("/").pop();
                vm.hashRequestResponse[answerId].callback(vm.hashRequestResponse[answerId].id,payload);
                // Unsubscribe
                vm.hashRequestResponse[answerId].subscription.unsubscribe();//Unsubscribe
              })
              // Send message
              $stomp.send(__env.socketEndpointSend + "/" + UUID, reqrespList[reqrest].msg)
            }
          })
        };

      vm.sendAndSubscribe = function(datasource){
        if(vm.connected){
          var UUID = generateConnectionUUID();
          vm.hashRequestResponse[UUID] = datasource;
          vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
            var answerId = headers.destination.split("/").pop();
            vm.hashRequestResponse[answerId].callback(vm.hashRequestResponse[answerId].id,vm.hashRequestResponse[answerId].type,payload);
            // Unsubscribe
            vm.hashRequestResponse[UUID].subscription.unsubscribe();//Unsubscribe
            function deleteHash(UUID){
              $timeout(
                function(){
                  delete vm.hashRequestResponse[UUID];
                },
                0
              );
            }
            deleteHash(UUID);
          })

          // Send message
          $stomp.send(__env.socketEndpointSend + "/" + UUID, datasource.msg)
        }
        else{
          vm.initQueue.push(datasource);
        }
      }


      vm.disconnect = function(reqrespList){
        $stomp.disconnect().then(function () {
          $log.info('disconnected')
        })
      }

      function generateConnectionUUID(){
        var newUUID = (new Date()).getTime() + Math.floor(((Math.random()*1000000)));
        while(newUUID in vm.hashRequestResponse){
          newUUID = generateConnectionUUID();
        }
        return newUUID;
      }
  };
})();

(function () {
  'use strict';

  InteractionService.$inject = ["$log", "__env", "$rootScope"];
  angular.module('s2DashboardFramework')
    .service('interactionService', InteractionService);

  /** @ngInject */
  function InteractionService($log, __env, $rootScope) {
    var vm = this;
    //Gadget interaction hash table, {gadgetsource:{emiterField:"field1", targetList: [{gadgetId,overwriteField}]}}
    vm.interactionHash = {

    };

    vm.setInteractionHash = function(interactionHash){
      vm.interactionHash = interactionHash;
    };

    vm.getInteractionHash = function(){
      return vm.interactionHash;
    };

    vm.registerGadget = function (gadgetId) {
      if(!(gadgetId in vm.interactionHash)){
        vm.interactionHash[gadgetId] = [];
      }
    };

    vm.unregisterGadget = function (gadgetId) {
      //Delete from sources list
      delete vm.interactionHash[gadgetId];
      //Delete from destination list
      for (var keyGadget in vm.interactionHash) {
        var destinationList = vm.interactionHash[keyGadget];
        for (var keyDest in destinationList) {
          var destinationFieldBundle = destinationList[keyDest];
          var found = -1; //-1 not found other remove that position in targetList array
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];
            if (destination.gadgetId == gadgetId) {
              found = keyGDest;
              break;
            }
          }
          //delete targetList entry if diferent -1
          if (found != -1) {
            destinationBundle.targetList.splice(found, 1);
          }
        }
      }
    };

    vm.registerGadgetFieldEmitter = function (gadgetId, fieldEmitter) {
      if(!(gadgetId in vm.interactionHash)){
        vm.interactionHash[gadgetId] = [];
      }
      if(!(vm.interactionHash[gadgetId].filter(function(f){return f.emiterField === fieldEmitter}).length)){
        vm.interactionHash[gadgetId].push(
          {
            targetList: [],
            emiterField: fieldEmitter
          }
        )
      }
    };

    vm.unregisterGadgetFieldEmitter = function (gadgetId, fieldEmitter) {
      var indexEmitter;
      vm.interactionHash[gadgetId].map(function (elem, index) {
        if (elem.fieldEmitter === fieldEmitter) {
          indexEmitter = index;
        }
      })
      vm.interactionHash[gadgetId].splice(found, 1);
    };

    vm.registerGadgetInteractionDestination = function (sourceGadgetId, targetGadgetId, originField, destinationField) {
      //Auto generated
      if(!(sourceGadgetId in vm.interactionHash) || !(vm.interactionHash[sourceGadgetId].filter(function(f){return f.emiterField === originField}).length)){
        vm.registerGadgetFieldEmitter(sourceGadgetId, originField);
      }
      var destinationFieldBundle = vm.interactionHash[sourceGadgetId].filter(
        function (elem) {
          return elem.emiterField == originField;
        }
      );
      destinationFieldBundle[0].targetList.push({
        gadgetId: targetGadgetId,
        overwriteField: destinationField
      })
    };

    vm.unregisterGadgetInteractionDestination = function (sourceGadgetId, targetGadgetId, originField, destinationField) {
      var destinationFieldBundle = vm.interactionHash[sourceGadgetId].filter(
        function (elem) {
          return elem.emiterField == originField;
        }
      );
      var found = -1;
      destinationFieldBundle[0].targetList.map(
        function (dest, index) {
          if (dest.overwriteField == destinationField && dest.gadgetId == targetGadgetId) {
            found = index;
          }
        }
      );
      if (found != -1) {
        destinationFieldBundle[0].targetList.splice(found, 1);
      }
    };

    //SourceFilterData: {"field1":"value1","field2":"value2","field3":"value3"}
    vm.sendBroadcastFilter = function (gadgetId, sourceFilterData) {
      var destinationList = vm.interactionHash[gadgetId];
      for (var keyDest in destinationList) {
        var destinationFieldBundle = destinationList[keyDest];
        //Target list is not empty and field came from triggered gadget data
        if (destinationFieldBundle.targetList.length > 0 && destinationFieldBundle.emiterField in sourceFilterData) {
          for (var keyGDest in destinationFieldBundle.targetList) {
            var destination = destinationFieldBundle.targetList[keyGDest];
            emitToTargets(destination.gadgetId, buildFilterEvent(destination, destinationFieldBundle, sourceFilterData, gadgetId));
          }
        }
      }
    };

    function buildFilterEvent(destination, destinationFieldBundle, sourceFilterData, gadgetEmitterId) {
      var sourceFilterDataAux = angular.copy(sourceFilterData);
      //we add first de filter event by the parent filter and then we add the chaining filter with the same filterId in order to propagate filters
      var dataFilter = [{
        "field": destination.overwriteField, 
        "value": sourceFilterData[destinationFieldBundle.emiterField].value
      }];
      delete sourceFilterDataAux[destinationFieldBundle.emiterField];
      for(var field in sourceFilterDataAux){
        dataFilter.push({
          "field": field, 
          "value": sourceFilterDataAux[field].value
        })
      }
      return {
        "type": "filter", 
        "id": gadgetEmitterId, 
        "data": dataFilter
      };
    }

    function emitToTargets(id, data) {
      $rootScope.$broadcast(id, data);
    }

  };
})();

(function () {
  'use strict';

  angular.module('s2DashboardFramework')
    .service('gadgetManagerService', GadgetManagerService);

  /** @ngInject */
  function GadgetManagerService() {
      var vm = this;
      vm.dashboardModel = {};
      vm.selectedpage = 0

      vm.setDashboardModelAndPage = function(dashboard,selectedpage){
        vm.dashboardModel = dashboard;
        vm.selectedpage = selectedpage;
      }

      vm.findGadgetById = function(gadgetId){
        var page = vm.dashboardModel.pages[vm.selectedpage];
        for(var layerIndex in page.layers){
          var layer = page.layers[layerIndex];
          var gadgets = layer.gridboard.filter(function(gadget){return gadget.id === gadgetId});
          if(gadgets.length){
            return gadgets[0];
          }
        }
        return null;
      }
      
  }
})();

(function () {
  'use strict';

  DatasourceSolverService.$inject = ["socketService", "sofia2HttpService", "$mdDialog", "$interval", "$rootScope"];
  angular.module('s2DashboardFramework')
    .service('datasourceSolverService', DatasourceSolverService);

  /** @ngInject */
  function DatasourceSolverService(socketService, sofia2HttpService, $mdDialog, $interval, $rootScope) {
      var vm = this;
      vm.gadgetToDatasource = {};
      
      vm.pendingDatasources = {};
      vm.poolingDatasources = {};
      vm.streamingDatasources = {};

      sofia2HttpService.setDashboardEngineCredentialsAndLogin().then(function(a){
        console.log("Login Rest OK, connecting SockJs Stomp dashboard engine");
        socketService.connect();
      }).catch(function(e){
        console.log("Dashboard Engine Login Rest Fail: " + JSON.stringify(e));
        $mdDialog.show(
          $mdDialog.alert()
            .parent(angular.element(document.querySelector('body')))
            .clickOutsideToClose(true)
            .title('Dashboard Engine Connection Fail')
            .textContent('Dashboard engine could not to be running, please start it and reload this page')
            .ariaLabel('Alert Dialog Dashboard Engine')
            .ok('OK')
            //.targetEvent(ev)
        );
      })

      //datasource {"name":"name","type":"query","refresh":"refresh",triggers:[{params:{where:[],project:[],filter:[]},emiter:""}]}
      vm.registerDatasources = function(datasources){
        for(var key in datasources){
          var datasource = datasources[key];
          if(datasource.type=="query"){//Query datasource. We don't need RT conection only request-response
            if(datasource.refresh==0){//One shot datasource, we don't need to save it, only execute it once
              //vm.pendingDatasources[datasource.name] = datasource;
              for(var i = 0; i< datasource.triggers.length;i++){
                socketService.connectAndSendAndSubscribe([{"msg":fromTriggerToMessage(datasource.triggers[i],datasource.name),id: datasource.triggers[i].emitTo, callback: vm.emitToTargets}]);
              }
            }
            else{//Interval query datasource, we need to register this datasource in order to pooling results
              vm.poolingDatasources[datasource.name] = datasource;
              var intervalId = $interval(/*Datasource passed as parameter in order to call every refresh time*/
                function(datasource){
                  for(var i = 0; i< datasource.triggers.length;i++){
                    socketService.connectAndSendAndSubscribe([{"msg":fromTriggerToMessage(datasource.triggers[i],datasource.name),id: datasource.triggers[i].emitTo, callback: vm.emitToTargets}]);
                  }
                },datasource.refresh * 1000, 0, true, datasource
              );
              vm.poolingDatasources[datasource.name].intervalId = intervalId;
            }
          }
          else{//Streaming datasource
            //TODO
          }
        }
      };

      function connectRegisterSingleDatasourceAndFirstShot(datasource){
        if(datasource.type=="query"){//Query datasource. We don't need RT conection only request-response
          if(datasource.refresh==0){//One shot datasource, we don't need to save it, only execute it once
            //vm.pendingDatasources[datasource.name] = datasource;
            for(var i = 0; i< datasource.triggers.length;i++){
              socketService.connectAndSendAndSubscribe([{"msg":fromTriggerToMessage(datasource.triggers[i],datasource.name),id: datasource.triggers[i].emitTo, callback: vm.emitToTargets}]);
            }
          }
          else{//Interval query datasource, we need to register this datasource in order to pooling results
            vm.poolingDatasources[datasource.name] = datasource;
            var intervalId = $interval(/*Datasource passed as parameter in order to call every refresh time*/
              function(datasource){
                for(var i = 0; i< datasource.triggers.length;i++){
                  socketService.connectAndSendAndSubscribe([{"msg":fromTriggerToMessage(datasource.triggers[i],datasource.name),id: datasource.triggers[i].emitTo, callback: vm.emitToTargets}]);
                }
              },datasource.refresh * 1000, 0, true, datasource
            );
            vm.poolingDatasources[datasource.name].intervalId = intervalId;
          }
        }
        else{//Streaming datasource
          //TODO
        }
      }

      //Method from gadget to drill up and down the datasource
      vm.drillDown = function(gadgetId){}
      vm.drillUp = function(gadgetId){}

      vm.updateDatasourceTriggerAndShot = function(gadgetID, updateInfo){
        var accessInfo = vm.gadgetToDatasource[gadgetID];
        var dsSolver = vm.poolingDatasources[accessInfo.ds].triggers[accessInfo.index];
        updateQueryParams(dsSolver,updateInfo);
        var solverCopy = angular.copy(dsSolver);
        solverCopy.params.filter = [];
        for(var index in dsSolver.params.filter){
          var bundleFilters = dsSolver.params.filter[index].data;
          for(var indexB in bundleFilters){
            solverCopy.params.filter.push(bundleFilters[indexB]);
          }
        }
        socketService.sendAndSubscribe({"msg":fromTriggerToMessage(solverCopy,accessInfo.ds),id: angular.copy(gadgetID), type:"filter", callback: vm.emitToTargets});
      }

      //update info has the filter, group, project id to allow override filters from same gadget and combining with others
      function updateQueryParams(trigger, updateInfo){
        var index = 0;//index filter
        var overwriteFilter = trigger.params.filter.filter(function(sfilter,i){
          if(sfilter.id == updateInfo.filter.id){
            index = i;
          }
          return sfilter.id == updateInfo.filter.id;
        });
        if (overwriteFilter.length>0){//filter founded, we need to override it
          if(updateInfo.filter.data.length==0){//with empty array we delete it, remove filter action
            trigger.params.filter.splice(index,1); 
          }
          else{ //override filter, for example change filter data and no adding
            overwriteFilter[0].data = updateInfo.filter.data;  
          }
        }
        else{
          trigger.params.filter.push(updateInfo.filter);
        }

        if(updateInfo.group){//For group that only change in drill options, we need to override all elements
          trigger.params.group = updateInfo.group;
        }

        if(updateInfo.project){//For project that only change in drill options, we need to override all elements
          trigger.params.project = updateInfo.project;
        }
      }

      vm.registerSingleDatasourceAndFirstShot = function(datasource){
        if(datasource.type=="query"){//Query datasource. We don't need RT conection only request-response
          if(!(datasource.name in vm.poolingDatasources)){
            vm.poolingDatasources[datasource.name] = datasource;
            vm.poolingDatasources[datasource.name].triggers[0].listeners = 1;
            vm.gadgetToDatasource[datasource.triggers[0].emitTo] = {"ds":datasource.name, "index":0};
          }
          else if(!(datasource.triggers[0].emitTo in vm.gadgetToDatasource)){
            vm.poolingDatasources[datasource.name].triggers.push(datasource.triggers[0]);
            var newposition = vm.poolingDatasources[datasource.name].triggers.length-1
            vm.poolingDatasources[datasource.name].triggers[newposition].listeners = 1;
            vm.gadgetToDatasource[datasource.triggers[0].emitTo] = {"ds":datasource.name, "index":newposition};
          }
          else{
            var gpos = vm.gadgetToDatasource[datasource.triggers[0].emitTo];
            vm.poolingDatasources[datasource.name].triggers[gpos.index].listeners++;
          }
          //One shot datasource, for pooling and 
          //vm.pendingDatasources[datasource.name] = datasource;
          for(var i = 0; i< datasource.triggers.length;i++){
            socketService.sendAndSubscribe({"msg":fromTriggerToMessage(datasource.triggers[i],datasource.name),id: angular.copy(datasource.triggers[i].emitTo), type:"refresh", callback: vm.emitToTargets});
          }
          if(datasource.refresh!=0){//Interval query datasource, we need to register this datasource in order to pooling results
            var i;
            var intervalId = $interval(/*Datasource passed as parameter in order to call every refresh time*/
              function(datasource){
                for(var i = 0; i< datasource.triggers.length;i++){
                  //Send filter array base without id filter (TODO)
                  var solverCopy = angular.copy(datasource.triggers[i]);
                  solverCopy.params.filter = [];
                  for(var index in datasource.triggers[i].params.filter){
                    var bundleFilters = datasource.triggers[i].params.filter[index].data;
                    for(var indexB in bundleFilters){
                      solverCopy.params.filter.push(bundleFilters[indexB]);
                    }
                  }
                  socketService.sendAndSubscribe({"msg":fromTriggerToMessage(solverCopy,datasource.name),id: angular.copy(datasource.triggers[i].emitTo), type:"refresh", callback: vm.emitToTargets});
                }
              },datasource.refresh * 1000, 0, true, datasource
            );
            vm.poolingDatasources[datasource.name].intervalId = intervalId;
          }
        }
        else{//Streaming datasource
          //TODO
        }
      }

      function fromTriggerToMessage(trigger,dsname){
        var baseMsg = trigger.params;
        baseMsg.ds = dsname;
        return baseMsg;
      }

      vm.emitToTargets = function(id,name,data){
        //pendingDatasources
        $rootScope.$broadcast(id,
          {
            type: "data",
            name: name,
            data: JSON.parse(data.data)
          }
        );
      }

      vm.registerDatasource = function(datasource){
        vm.poolingDatasources[datasource.name] = datasource;
      }

      vm.registerDatasourceTrigger = function(datasource, trigger){//add streaming too
        if(!(datasource.name in vm.poolingDatasources)){
          vm.poolingDatasources[datasource.name] = datasource;
        }
        vm.poolingDatasources[name].triggers.push(trigger);
        //trigger one shot
      }

      vm.unregisterDatasourceTrigger = function(name,emiter){

        if(name in vm.pendingDatasources && vm.pendingDatasources[name].triggers.length == 0){
          vm.pendingDatasources[name].triggers = vm.pendingDatasources[name].triggers.filter(function(trigger){return trigger.emitTo!=emiter});

          if(vm.pendingDatasources[name].triggers.length==0){
            delete vm.pendingDatasources[name];
          }
        }
        if(name in vm.poolingDatasources && vm.poolingDatasources[name].triggers.length == 0){
          var trigger = vm.poolingDatasources[name].triggers.filter(function(trigger){return trigger.emitTo==emiter});
          trigger.listeners--;
          if(trigger.listeners==0){
            vm.poolingDatasources[name].triggers = vm.poolingDatasources[name].triggers.filter(function(trigger){return trigger.emitTo!=emiter});
          }

          if(vm.poolingDatasources[name].triggers.length==0){
            $interval.clear(vm.poolingDatasources[datasource.name].intervalId);
            delete vm.poolingDatasources[name];
          }
        }
        if(name in vm.streamingDatasources && vm.streamingDatasources[name].triggers.length == 0){
          vm.streamingDatasources[name].triggers = vm.streamingDatasources[name].triggers.filter(function(trigger){return trigger.emitTo!=emiter});

          if(vm.streamingDatasources[name].triggers.length==0){
            /*Unsubsuscribe TODO*/
            delete vm.streamingDatasources[name];
          }
        }
      }

      vm.disconnect = function(){
        socketService.disconnect();
      }
  }
})();

(function () {
    'use strict';

    RetryHttpProviderConfig.$inject = ["$httpProvider"];
    angular.module('s2DashboardFramework').config(RetryHttpProviderConfig);

    /** @ngInject */
    function RetryHttpProviderConfig($httpProvider) {  
        $httpProvider.interceptors.push(["$q", "$injector", function ($q, $injector) {
            var incrementalTimeout = 1000;
        
            function retryRequest (httpConfig) {
                var $timeout = $injector.get('$timeout');
                var thisTimeout = incrementalTimeout;
                incrementalTimeout *= 2;
                return $timeout(function() {
                    var $http = $injector.get('$http');
                    return $http(httpConfig);
                }, thisTimeout);
            };
        
            return {
                responseError: function (response) {
                    if (response.status === 500) {
                        if (incrementalTimeout < 5000) {
                            return retryRequest(response.config);
                        }
                        else {
                            console.error('The remote server seems to be busy at the moment. Please try again in later');
                        }
                    }
                    else {
                        incrementalTimeout = 1000;
                    }
                    return $q.reject(response);
                }
            };
        }]); 
    }

})();
!function(e,i,n){"use strict";var t=function(){return"lfobjyxxxxxxxx".replace(/[xy]/g,function(e){var i=16*Math.random()|0,n="x"==e?i:3&i|8;return n.toString(16)})},l=function(e){var i=e.type,n=e.name;return o(i,n)?"image":r(i,n)?"video":s(i,n)?"audio":"object"},o=function(e,i){return!(!e.match("image.*")&&!i.match(/\.(gif|png|jpe?g)$/i))},r=function(e,i){return!(!e.match("video.*")&&!i.match(/\.(og?|mp4|webm|3gp)$/i))},s=function(e,i){return!(!e.match("audio.*")&&!i.match(/\.(ogg|mp3|wav)$/i))},a=function(i){var n={key:t(),lfFile:i,lfFileName:i.name,lfFileType:i.type,lfTagType:l(i),lfDataUrl:e.URL.createObjectURL(i),isRemote:!1};return n},f=function(e,i,n){var o={name:i,type:n},r={key:t(),lfFile:void 0,lfFileName:i,lfFileType:n,lfTagType:l(o),lfDataUrl:e,isRemote:!0};return r},c=i.module("lfNgMdFileInput",["ngMaterial"]);c.directive("lfFile",function(){return{restrict:"E",scope:{lfFileObj:"=",lfUnknowClass:"="},link:function(e,i,n){var t=e.lfFileObj.lfDataUrl,l=e.lfFileObj.lfFileType,o=e.lfFileObj.lfTagType,r=e.lfUnknowClass;switch(o){case"image":i.replaceWith('<img src="'+t+'" />');break;case"video":i.replaceWith('<video controls><source src="'+t+'""></video>');break;case"audio":i.replaceWith('<audio controls><source src="'+t+'""></audio>');break;default:void 0==e.lfFileObj.lfFile&&(l="unknown/unknown"),i.replaceWith('<object type="'+l+'" data="'+t+'"><div class="lf-ng-md-file-input-preview-default"><md-icon class="lf-ng-md-file-input-preview-icon '+r+'"></md-icon></div></object>')}}}}),c.run(["$templateCache",function(e){e.put("lfNgMdFileinput.html",['<div layout="column" class="lf-ng-md-file-input" ng-model="'+t()+'">','<div layout="column" class="lf-ng-md-file-input-preview-container" ng-class="{\'disabled\':isDisabled}" ng-show="isDrag || (isPreview && lfFiles.length)">','<md-button aria-label="remove all files" class="close lf-ng-md-file-input-x" ng-click="removeAllFiles($event)" ng-hide="!lfFiles.length || !isPreview" >&times;</md-button>','<div class="lf-ng-md-file-input-drag">','<div layout="row" layout-align="center center" class="lf-ng-md-file-input-drag-text-container" ng-show="(!lfFiles.length || !isPreview) && isDrag">','<div class="lf-ng-md-file-input-drag-text">{{strCaptionDragAndDrop}}</div>',"</div>",'<div class="lf-ng-md-file-input-thumbnails" ng-if="isPreview == true">','<div class="lf-ng-md-file-input-frame" ng-repeat="lffile in lfFiles" ng-click="onFileClick(lffile)">','<div class="lf-ng-md-file-input-x" aria-label="remove {{lffile.lFfileName}}" ng-click="removeFile(lffile,$event)">&times;</div>','<lf-file lf-file-obj="lffile" lf-unknow-class="strUnknowIconCls"/>','<div class="lf-ng-md-file-input-frame-footer">','<div class="lf-ng-md-file-input-frame-caption">{{lffile.lfFileName}}</div>',"</div>","</div>","</div>",'<div class="clearfix" style="clear:both"></div>',"</div>","</div>",'<div layout="row" class="lf-ng-md-file-input-container" >','<div class="lf-ng-md-file-input-caption" layout="row" layout-align="start center" flex ng-class="{\'disabled\':isDisabled}" >','<md-icon class="lf-icon" ng-class="strCaptionIconCls"></md-icon>','<div flex class="lf-ng-md-file-input-caption-text-default" ng-show="!lfFiles.length">',"{{strCaptionPlaceholder}}","</div>",'<div flex class="lf-ng-md-file-input-caption-text" ng-hide="!lfFiles.length">','<span ng-if="isCustomCaption">{{strCaption}}</span>','<span ng-if="!isCustomCaption">','{{ lfFiles.length == 1 ? lfFiles[0].lfFileName : lfFiles.length+" files selected" }}',"</span>","</div>",'<md-progress-linear md-mode="determinate" value="{{floatProgress}}" ng-show="intLoading && isProgress"></md-progress-linear>',"</div>",'<md-button aria-label="remove all files" ng-disabled="isDisabled" ng-click="removeAllFiles()" ng-hide="!lfFiles.length || intLoading" class="md-raised lf-ng-md-file-input-button lf-ng-md-file-input-button-remove" ng-class="strRemoveButtonCls">','<md-icon class="lf-icon" ng-class="strRemoveIconCls"></md-icon> ',"{{strCaptionRemove}}","</md-button>",'<md-button aria-label="submit" ng-disabled="isDisabled" ng-click="onSubmitClick()" class="md-raised md-warn lf-ng-md-file-input-button lf-ng-md-file-input-button-submit" ng-class="strSubmitButtonCls" ng-show="lfFiles.length && !intLoading && isSubmit">','<md-icon class="lf-icon" ng-class="strSubmitIconCls"></md-icon> ',"{{strCaptionSubmit}}","</md-button>",'<md-button aria-label="browse" ng-disabled="isDisabled" ng-click="openDialog($event, this)" class="md-raised lf-ng-md-file-input-button lf-ng-md-file-input-button-brower" ng-class="strBrowseButtonCls">','<md-icon class="lf-icon" ng-class="strBrowseIconCls"></md-icon> ',"{{strCaptionBrowse}}",'<input type="file" aria-label="{{strAriaLabel}}" accept="{{accept}}" ng-disabled="isDisabled" class="lf-ng-md-file-input-tag" />',"</md-button>","</div>","</div>"].join(""))}]),c.filter("lfTrusted",["$sce",function(e){return function(i){return e.trustAsResourceUrl(i)}}]),c.directive("lfRequired",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){t&&(t.$validators.required=function(e,i){return e?e.length>0:!1})}}}),c.directive("lfMaxcount",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){if(t){var l=-1;n.$observe("lfMaxcount",function(e){var i=parseInt(e,10);l=isNaN(i)?-1:i,t.$validate()}),t.$validators.maxcount=function(e,i){return e?e.length<=l:!1}}}}}),c.directive("lfFilesize",function(){return{restrict:"A",require:"ngModel",link:function(e,i,n,t){if(t){var l=-1;n.$observe("lfFilesize",function(e){var i=/^[1-9][0-9]*(Byte|KB|MB)$/;if(i.test(e)){var n=["Byte","KB","MB"],o=e.match(i)[1],r=e.substring(0,e.indexOf(o));n.every(function(e,i){return o===e?(l=parseInt(r)*Math.pow(1024,i),!1):!0})}else l=-1;t.$validate()}),t.$validators.filesize=function(e,i){if(!e)return!1;var n=!0;return e.every(function(e,i){return e.lfFile.size>l?(n=!1,!1):!0}),n}}}}}),c.directive("lfTotalsize",function(){return{restrict:"A",require:"ngModel",link:function(e,n,t,l){if(l){var o=-1;t.$observe("lfTotalsize",function(e){var i=/^[1-9][0-9]*(Byte|KB|MB)$/;if(i.test(e)){var n=["Byte","KB","MB"],t=e.match(i)[1],r=e.substring(0,e.indexOf(t));n.every(function(e,i){return t===e?(o=parseInt(r)*Math.pow(1024,i),!1):!0})}else o=-1;l.$validate()}),l.$validators.totalsize=function(e,n){if(!e)return!1;var t=0;return i.forEach(e,function(e,i){t+=e.lfFile.size}),o>t}}}}}),c.directive("lfMimetype",function(){return{restrict:"A",require:"ngModel",link:function(e,i,t,l){if(l){var o;t.$observe("lfMimetype",function(e){var i=e.replace(/,/g,"|");o=new RegExp(i,"i"),l.$validate()}),l.$validators.mimetype=function(e,i){if(!e)return!1;var t=!0;return e.every(function(e,i){return e.lfFile!==n&&e.lfFile.type.match(o)?!0:(t=!1,!1)}),t}}}}}),c.directive("lfNgMdFileInput",["$q","$compile","$timeout",function(e,t,l){return{restrict:"E",templateUrl:"lfNgMdFileinput.html",replace:!0,require:"ngModel",scope:{lfFiles:"=?",lfApi:"=?",lfOption:"=?",lfCaption:"@?",lfPlaceholder:"@?",lfDragAndDropLabel:"@?",lfBrowseLabel:"@?",lfRemoveLabel:"@?",lfSubmitLabel:"@?",lfOnFileClick:"=?",lfOnSubmitClick:"=?",lfOnFileRemove:"=?",accept:"@?",ngDisabled:"=?",ngChange:"&?"},link:function(t,o,r,s){var c=i.element(o[0].querySelector(".lf-ng-md-file-input-tag")),u=i.element(o[0].querySelector(".lf-ng-md-file-input-drag")),d=i.element(o[0].querySelector(".lf-ng-md-file-input-thumbnails")),m=0;t.intLoading=0,t.floatProgress=0,t.isPreview=!1,t.isDrag=!1,t.isMutiple=!1,t.isProgress=!1,t.isCustomCaption=!1,t.isSubmit=!1,i.isDefined(r.preview)&&(t.isPreview=!0),i.isDefined(r.drag)&&(t.isDrag=!0),i.isDefined(r.multiple)?(c.attr("multiple","multiple"),t.isMutiple=!0):c.removeAttr("multiple"),i.isDefined(r.progress)&&(t.isProgress=!0),i.isDefined(r.submit)&&(t.isSubmit=!0),t.isDisabled=!1,i.isDefined(r.ngDisabled)&&t.$watch("ngDisabled",function(e){t.isDisabled=e}),t.strBrowseIconCls="lf-browse",t.strRemoveIconCls="lf-remove",t.strCaptionIconCls="lf-caption",t.strSubmitIconCls="lf-submit",t.strUnknowIconCls="lf-unknow",t.strBrowseButtonCls="md-primary",t.strRemoveButtonCls="",t.strSubmitButtonCls="md-accent",i.isDefined(r.lfOption)&&i.isObject(t.lfOption)&&(t.lfOption.hasOwnProperty("browseIconCls")&&(t.strBrowseIconCls=t.lfOption.browseIconCls),t.lfOption.hasOwnProperty("removeIconCls")&&(t.strRemoveIconCls=t.lfOption.removeIconCls),t.lfOption.hasOwnProperty("captionIconCls")&&(t.strCaptionIconCls=t.lfOption.captionIconCls),t.lfOption.hasOwnProperty("unknowIconCls")&&(t.strUnknowIconCls=t.lfOption.unknowIconCls),t.lfOption.hasOwnProperty("submitIconCls")&&(t.strSubmitIconCls=t.lfOption.submitIconCls),t.lfOption.hasOwnProperty("strBrowseButtonCls")&&(t.strBrowseButtonCls=t.lfOption.strBrowseButtonCls),t.lfOption.hasOwnProperty("strRemoveButtonCls")&&(t.strRemoveButtonCls=t.lfOption.strRemoveButtonCls),t.lfOption.hasOwnProperty("strSubmitButtonCls")&&(t.strSubmitButtonCls=t.lfOption.strSubmitButtonCls)),t.accept=t.accept||"",t.lfFiles=[],t[r.ngModel]=t.lfFiles,t.lfApi=new function(){var e=this;e.removeAll=function(){t.removeAllFiles()},e.removeByName=function(e){t.removeFileByName(e)},e.addRemoteFile=function(e,i,n){var l=f(e,i,n);t.lfFiles.push(l)}},t.strCaption="",t.strCaptionPlaceholder="Select file",t.strCaptionDragAndDrop="Drag & drop files here...",t.strCaptionBrowse="Browse",t.strCaptionRemove="Remove",t.strCaptionSubmit="Submit",t.strAriaLabel="",i.isDefined(r.ariaLabel)&&(t.strAriaLabel=r.ariaLabel),i.isDefined(r.lfPlaceholder)&&t.$watch("lfPlaceholder",function(e){t.strCaptionPlaceholder=e}),i.isDefined(r.lfCaption)&&(t.isCustomCaption=!0,t.$watch("lfCaption",function(e){t.strCaption=e})),t.lfDragAndDropLabel&&(t.strCaptionDragAndDrop=t.lfDragAndDropLabel),t.lfBrowseLabel&&(t.strCaptionBrowse=t.lfBrowseLabel),t.lfRemoveLabel&&(t.strCaptionRemove=t.lfRemoveLabel),t.lfSubmitLabel&&(t.strCaptionSubmit=t.lfSubmitLabel),t.openDialog=function(e,i){e&&l(function(){e.preventDefault(),e.stopPropagation();var i=e.target.children[2];i!==n&&c[0].click()},0)},t.removeAllFilesWithoutVaildate=function(){t.isDisabled||(t.lfFiles.length=0,d.empty())},t.removeAllFiles=function(e){t.removeAllFilesWithoutVaildate(),g()},t.removeFileByName=function(e,i){t.isDisabled||(t.lfFiles.every(function(i,n){return i.lfFileName==e?(t.lfFiles.splice(n,1),!1):!0}),g())},t.removeFile=function(e){t.lfFiles.every(function(n,l){return n.key==e.key?(i.isFunction(t.lfOnFileRemove)&&t.lfOnFileRemove(n,l),t.lfFiles.splice(l,1),!1):!0}),g()},t.onFileClick=function(e){i.isFunction(t.lfOnFileClick)&&t.lfFiles.every(function(i,n){return i.key==e.key?(t.lfOnFileClick(i,n),!1):!0})},t.onSubmitClick=function(){i.isFunction(t.lfOnSubmitClick)&&t.lfOnSubmitClick(t.lfFiles)},u.bind("dragover",function(e){e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag&&u.addClass("lf-ng-md-file-input-drag-hover")}),u.bind("dragleave",function(e){e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag&&u.removeClass("lf-ng-md-file-input-drag-hover")}),u.bind("drop",function(e){if(e.stopPropagation(),e.preventDefault(),!t.isDisabled&&t.isDrag){u.removeClass("lf-ng-md-file-input-drag-hover"),i.isObject(e.originalEvent)&&(e=e.originalEvent);var n=e.target.files||e.dataTransfer.files,l=t.accept.replace(/,/g,"|"),o=new RegExp(l,"i"),r=[];i.forEach(n,function(e,i){e.type.match(o)&&r.push(e)}),p(r)}}),c.bind("change",function(e){var i=e.files||e.target.files;p(i)});var p=function(e){if(!(e.length<=0)){t.lfFiles.map(function(e){return e.lfFileName});if(t.floatProgress=0,t.isMutiple){m=e.length,t.intLoading=m;for(var i=0;i<e.length;i++){var n=e[i];setTimeout(v(n),100*i)}}else{m=1,t.intLoading=m;for(var i=0;i<e.length;i++){var n=e[i];t.removeAllFilesWithoutVaildate(),v(n);break}}c.val("")}},g=function(){i.isFunction(t.ngChange)&&t.ngChange(),s.$validate()},v=function(e){b(e).then(function(i){var l=!1;if(t.lfFiles.every(function(i,t){var o=i.lfFile;return i.isRemote?!0:o.name!==n&&o.name==e.name?(o.size==e.size&&o.lastModified==e.lastModified&&(l=!0),!1):!0}),!l){var o=a(e);t.lfFiles.push(o)}0==t.intLoading&&g()},function(e){},function(e){})},b=function(i,n){var l=e.defer(),o=new FileReader;return o.onloadstart=function(){l.notify(0)},o.onload=function(e){},o.onloadend=function(e){l.resolve({index:n,result:o.result}),t.intLoading--,t.floatProgress=(m-t.intLoading)/m*100},o.onerror=function(e){l.reject(o.result),t.intLoading--,t.floatProgress=(m-t.intLoading)/m*100},o.onprogress=function(e){l.notify(e.loaded/e.total)},o.readAsArrayBuffer(i),l.promise}}}}])}(window,window.angular);
var env = {};

// Import variables if present (from env.js)
if(window && window.__env){
  Object.assign(env, window.__env);
}
else{//Default config
  env.socketEndpointConnect = '/dashboardengine/dsengine/solver';
  env.socketEndpointSend = '/dsengine/solver';
  env.socketEndpointSubscribe = '/dsengine/broker';
  env.endpointSofia2ControlPanel = '/controlpanel';
  env.endpointSofia2DashboardEngine = '/dashboardengine';
  env.enableDebug = false;
  env.dashboardEngineUsername = '';
  env.dashboardEnginePassword = '';
  env.dashboardEngineLoginRest = '/loginRest';
  env.dashboardEngineLoginRestTimeout = 5000;
  env.sofia2RestUrl = 'http://rancher.sofia4cities.com/iotbroker/rest/client';
}

angular.module('s2DashboardFramework').constant('__env', env);

(function () {
  'use strict';

  angular.module('s2DashboardFramework').directive('draggable', function() {
    return function(scope, element) {
      // this gives us the native JS object
      var el = element[0];

      el.draggable = true;

      el.addEventListener(
        'dragstart',
        function(e) {
          e.dataTransfer.effectAllowed = 'move';
          e.dataTransfer.setData('type', this.id);
          this.classList.add('drag');
          return false;
        },
        false
      );

      el.addEventListener(
        'dragend',
        function(e) {
          this.classList.remove('drag');
          return false;
        },
        false
      );
    }
  });
})();

(function () {
  'use strict';

  config.$inject = ["$logProvider", "$compileProvider"];
  angular.module('s2DashboardFramework').config(config);

  /** @ngInject */
  function config($logProvider, $compileProvider) {
    // Disable debug
    $logProvider.debugEnabled(true);
    $compileProvider.debugInfoEnabled(true);

  }

})();

(function () {
  'use strict';

  MainController.$inject = ["$log", "$scope", "$mdSidenav", "$mdDialog", "$timeout", "$window", "sofia2HttpService", "interactionService", "gadgetManagerService"];
  angular.module('s2DashboardFramework')
    .component('s2dashboard', {
      templateUrl: 'app/s2dashboard.html',
      controller: MainController,
      controllerAs: 'vm',
      bindings:{
        editmode : "=",
        selectedpage : "&",
        id: "@",
        public: "="
      }
    });

  /** @ngInject */
  function MainController($log, $scope, $mdSidenav, $mdDialog, $timeout, $window, sofia2HttpService, interactionService, gadgetManagerService) {
    var vm = this;
    vm.$onInit = function () {
      setTimeout(function () {
        vm.sidenav = $mdSidenav('left');
      }, 100);
      vm.selectedpage=0;

      /*Rest api call to get dashboard data*/
      sofia2HttpService.getDashboardModel(vm.id).then(
        function(model){
          vm.dashboard = model.data;

          vm.dashboard.gridOptions.resizable.stop = sendResizeToGadget;

          vm.dashboard.gridOptions.enableEmptyCellDrop = true;
          vm.dashboard.gridOptions.emptyCellDropCallback = dropElementEvent.bind(this);

          //If interaction hash then recover connections
          if(vm.dashboard.interactionHash){
            interactionService.setInteractionHash(vm.dashboard.interactionHash);
          }
          vm.dashboard.gridOptions.displayGrid = "none";
          if(!vm.editmode){           
            vm.dashboard.gridOptions.draggable.enabled = false;
            vm.dashboard.gridOptions.resizable.enabled = false;
            vm.dashboard.gridOptions.enableEmptyCellDrop = false;
          }
          gadgetManagerService.setDashboardModelAndPage(vm.dashboard,vm.selectedpage);
        }
      ).catch(
        function(){
          $window.location.href = "/controlpanel/login";
        }
      )

      function showAddGadgetDialog(type,config,layergrid){
        AddGadgetController.$inject = ["$scope", "$mdDialog", "sofia2HttpService", "type", "config", "layergrid"];
        function AddGadgetController($scope, $mdDialog, sofia2HttpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;

          $scope.gadgets = [];
         

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

          $scope.loadGadgets = function() {
            return sofia2HttpService.getUserGadgetsByType($scope.type).then(
              function(gadgets){
                $scope.gadgets = gadgets.data;
              }
            );
          };

          $scope.addGadget = function() {
            $scope.config.type = $scope.type;
            $scope.config.id = $scope.gadget.id;
            $scope.config.header.title.text = $scope.gadget.identification;
            $scope.layergrid.push($scope.config);
            $mdDialog.cancel();
          };
        }

        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }


      function showAddGadgetTemplateDialog(type,config,layergrid){
        AddGadgetController.$inject = ["$scope", "$mdDialog", "sofia2HttpService", "type", "config", "layergrid"];
        function AddGadgetController($scope, $mdDialog, sofia2HttpService, type, config, layergrid) {
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;

         
          $scope.templates = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

         
          $scope.loadTemplates = function() {
            return sofia2HttpService.getUserGadgetTemplate().then(
              function(templates){
                $scope.templates = templates.data;
              }
            );
          };

          $scope.useTemplate = function() {    
                 
            $scope.config.type = $scope.type;
            $scope.config.content=$scope.template.template          
            showAddGadgetTemplateParameterDialog($scope.type,$scope.config,$scope.layergrid);
            $mdDialog.hide();
          };
          $scope.noUseTemplate = function() {
            $scope.config.type = $scope.type;        
            $scope.layergrid.push($scope.config);
            $mdDialog.cancel();
          };

        }
        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetTemplateDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {
       
        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }



      function showAddGadgetTemplateParameterDialog(type,config,layergrid){
        AddGadgetController.$inject = ["$scope", "$mdDialog", "$mdCompiler", "sofia2HttpService", "type", "config", "layergrid"];
        function AddGadgetController($scope, $mdDialog,$mdCompiler, sofia2HttpService, type, config, layergrid) {
          var agc = this;
          agc.$onInit = function () {
            $scope.loadDatasources();
            $scope.getPredefinedParameters();
          }
         
          $scope.type = type;
          $scope.config = config;
          $scope.layergrid = layergrid;
          $scope.datasource;
          $scope.datasources = [];
          $scope.datasourceFields = [];
          $scope.parameters = [];
         
          $scope.templates = [];

          $scope.hide = function() {
            $mdDialog.hide();
          };

          $scope.cancel = function() {
            $mdDialog.cancel();
          };

         
          $scope.loadDatasources = function(){
            return sofia2HttpService.getDatasources().then(
              function(response){
                $scope.datasources=response.data;
                
              },
              function(e){
                console.log("Error getting datasources: " +  JSON.stringify(e))
              }
            );
          };
    
          $scope.iterate=  function (obj, stack, fields) {
            for (var property in obj) {
                 if (obj.hasOwnProperty(property)) {
                     if (typeof obj[property] == "object") {
                      $scope.iterate(obj[property], stack + (stack==""?'':'.') + property, fields);
              } else {
                         fields.push({field:stack + (stack==""?'':'.') + property, type:typeof obj[property]});
                     }
                 }
              }    
              return fields;
           }

          /**method that finds the tags in the given text*/
          function searchTag(regex,str){
            var m;
            var found=[];
            while ((m = regex.exec(str)) !== null) {  
                if (m.index === regex.lastIndex) {
                    regex.lastIndex++;
                }
                m.forEach(function(item, index, arr){			
                found.push(arr[0]);			
              });  
            }
            return found;
          }
          /**method that finds the name attribute and returns its value in the given tag */
          function searchTagContentName(regex,str){
            var m;
            var content;
            while ((m = regex.exec(str)) !== null) {  
                if (m.index === regex.lastIndex) {
                    regex.lastIndex++;
                }
                m.forEach(function(item, index, arr){			
                  content = arr[0].match(/"([^"]+)"/)[1];			
              });  
            }
            return content;
          }
          /**method that finds the options attribute and returns its values in the given tag */
          function searchTagContentOptions(regex,str){
            var m;
            var content=" ";
            while ((m = regex.exec(str)) !== null) {  
                if (m.index === regex.lastIndex) {
                    regex.lastIndex++;
                }
                m.forEach(function(item, index, arr){			
                  content = arr[0].match(/"([^"]+)"/)[1];			
              });  
            }
          
            return  content.split(',');
          }

          /**we look for the parameters in the source code to create the form */
          $scope.getPredefinedParameters = function(){
            var str =  $scope.config.content;
           	var regexTag =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
		        var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
            var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
		        var found=[];
	        	found = searchTag(regexTag,str);	
        
        
            for (var i = 0; i < found.length; i++) {			
              var tag = found[i];
              if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){	
                $scope.parameters.push({label:searchTagContentName(regexName,tag),value:"parameterTextLabel", type:"labelsText"});
              }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){
                $scope.parameters.push({label:searchTagContentName(regexName,tag),value:0, type:"labelsNumber"});              
              }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){
                $scope.parameters.push({label:searchTagContentName(regexName,tag),value:"parameterDsLabel", type:"labelsds"});               
              }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){
                $scope.parameters.push({label:searchTagContentName(regexName,tag),value:"parameterNameDsLabel", type:"labelsdspropertie"});               
              }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-s4c')>=0){
                var optionsValue = searchTagContentOptions(regexOptions,tag); 
                $scope.parameters.push({label:searchTagContentName(regexName,tag),value:"parameterSelectLabel",type:"selects", optionsValue:optionsValue});	              
              }
             } 
            }
        

            /**find a value for a given parameter */
            function findValueForParameter(label){
                for (var index = 0; index <  $scope.parameters.length; index++) {
                  var element =  $scope.parameters[index];
                  if(element.label===label){
                    return element.value;
                  }
                }
            }
        
            /**Parse the parameter of the data source so that it has array coding*/
            function parseArrayPosition(str){
              var regex = /\.[\d]+/g;
              var m;              
              while ((m = regex.exec(str)) !== null) {                
                  if (m.index === regex.lastIndex) {
                      regex.lastIndex++;
                  } 
                  m.forEach( function(item, index, arr){             
                    var index = arr[0].substring(1,arr[0].length)
                    var result =  "["+index+"]";
                    str = str.replace(arr[0],result) ;
                  });
              }
              return str;
            }

            /** this function Replace parameteres for his selected values*/
            function parseProperties(){
              var str =  $scope.config.content;
              var regexTag =  /<![\-\-\s\w\>\=\"\'\,\:\+\_\/]*\-->/g;
              var regexName = /name\s*=\s*\"[\s\w\>\=\-\'\+\_\/]*\s*\"/g;
              var regexOptions = /options\s*=\s*\"[\s\w\>\=\-\'\:\,\+\_\/]*\s*\"/g;
              var found=[];
              found = searchTag(regexTag,str);	
          
              var parserList=[];
              for (var i = 0; i < found.length; i++) {
                var tag = found[i];			
               
                if(tag.replace(/\s/g, '').search('type="text"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){                 
                  parserList.push({tag:tag,value:findValueForParameter(searchTagContentName(regexName,tag))});   
                }else if(tag.replace(/\s/g, '').search('type="number"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){
                  parserList.push({tag:tag,value:findValueForParameter(searchTagContentName(regexName,tag))});   
                }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){                
                  var field = parseArrayPosition(findValueForParameter(searchTagContentName(regexName,tag)).field);                               
                  parserList.push({tag:tag,value:"{{ds[0]."+field+"}}"});        
                }else if(tag.replace(/\s/g, '').search('type="ds_parameter"')>=0 && tag.replace(/\s/g, '').search('label-s4c')>=0){                
                  var field = parseArrayPosition(findValueForParameter(searchTagContentName(regexName,tag)).field);                               
                  parserList.push({tag:tag,value:field});        
                }else if(tag.replace(/\s/g, '').search('type="ds"')>=0 && tag.replace(/\s/g, '').search('select-s4c')>=0){                
                  parserList.push({tag:tag,value:findValueForParameter(searchTagContentName(regexName,tag))});  
                }
              } 
              //Replace parameteres for his values
              for (var i = 0; i < parserList.length; i++) {
                str = str.replace(parserList[i].tag,parserList[i].value);
              }
              return str;
            }
          
          



      
          $scope.loadDatasourcesFields = function(){
            
            if($scope.config.datasource!=null && $scope.config.datasource.id!=null && $scope.config.datasource.id!=""){
                 return sofia2HttpService.getsampleDatasources($scope.config.datasource.id).then(
                  function(response){
                    $scope.datasourceFields=$scope.iterate(response.data[0],"", []);
                  },
                  function(e){
                    console.log("Error getting datasourceFields: " +  JSON.stringify(e))
                  }
                );
              }
              else 
              {return null;}
        }


          $scope.save = function() { 
            $scope.config.type = $scope.type;
            $scope.config.content=parseProperties();            
            $scope.layergrid.push($scope.config);
            $mdDialog.cancel();
          };
        
        }
        $mdDialog.show({
          controller: AddGadgetController,
          templateUrl: 'app/partials/edit/addGadgetTemplateParameterDialog.html',
          parent: angular.element(document.body),
          clickOutsideToClose:true,
          fullscreen: false, // Only for -xs, -sm breakpoints.
          openFrom: '.sidenav-fab',
          closeTo: angular.element(document.querySelector('.sidenav-fab')),
          locals: {
            type: type,
            config: config,
            layergrid: layergrid
          }
        })
        .then(function() {

        }, function() {
          $scope.status = 'You cancelled the dialog.';
        });
      }






      function dropElementEvent(e,newElem){
        var type = e.dataTransfer.getData("type");
        newElem.id = type + "_" + (new Date()).getTime();
        newElem.content = type;
        newElem.type = type;
        newElem.header = {
          enable: true,
          title: {
            icon: "",
            iconColor: "hsl(220, 23%, 20%)",
            text: type + "_" + (new Date()).getTime(),
            textColor: "hsl(220, 23%, 20%)"
          },
          backgroundColor: "hsl(0, 0%, 100%)",
          height: "25"
        }
        newElem.backgroundColor ="white";
        newElem.padding = 0;
        newElem.border = {
          color: "#c7c7c7de",
          width: 1,
          radius: 5
        }
        if(type == 'livehtml'){         
          showAddGadgetTemplateDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard); 
        }
        else{
          showAddGadgetDialog(type,newElem,vm.dashboard.pages[vm.selectedpage].layers[vm.dashboard.pages[vm.selectedpage].selectedlayer].gridboard);
        }
      };


      function sendResizeToGadget(item, itemComponent) {
        $timeout(
          function(){
            $scope.$broadcast("$resize", "");
          },100
        );
      }
    };

    vm.checkIndex = function(index){
      return vm.selectedpage === index;
    }

    vm.setIndex = function(index){
      vm.selectedpage = index;
    }
  }
})();

angular.module('s2DashboardFramework').run(['$templateCache', function($templateCache) {$templateCache.put('app/s2dashboard.html','<edit-dashboard ng-if=vm.editmode id=vm.id public=vm.public dashboard=vm.dashboard selectedpage=vm.selectedpage></edit-dashboard><ng-include src="\'app/partials/view/header.html\'"></ng-include><ng-include src="\'app/partials/view/sidenav.html\'"></ng-include><span><div ng-style="vm.dashboard.header.enable && {\'height\': \'calc(100% - \'+{{vm.dashboard.header.height}}+\'px\'+\')\'} || {\'height\': \'100%\'}" ng-repeat="page in vm.dashboard.pages" ng-if=vm.checkIndex($index)><page page=page gridoptions=vm.dashboard.gridOptions dashboardheader=vm.dashboard.header editmode=vm.editmode selectedlayer=vm.selectedlayer class=flex ng-if=vm.checkIndex($index)></page></div></span>');
$templateCache.put('app/partials/edit/addGadgetDialog.html','<md-dialog aria-label="Add Gadget"><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Select Gadget to add</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-input-container><label>Select gadget type</label><md-select ng-model=gadget md-on-open=loadGadgets()><md-option ng-value=gadget ng-repeat="gadget in gadgets"><em>{{gadget.identification}}</em></md-option></md-select></md-input-container><md-dialog-actions layout=row><span flex></span><md-button ng-click=cancel()>Cancel</md-button><md-button ng-click=addGadget()>Add Gadget</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addGadgetTemplateDialog.html','<md-dialog aria-label="Add Gadget"><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Create using template?</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-input-container><label>Select Template</label><md-select ng-model=template md-on-open=loadTemplates()><md-option ng-value=template ng-repeat="template in templates"><em>{{template.identification}}</em></md-option></md-select></md-input-container><md-dialog-actions layout=row><span flex></span><md-button ng-click=useTemplate()>Yes</md-button><md-button ng-click=noUseTemplate()>No</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addGadgetTemplateParameterDialog.html','<md-dialog aria-label="Add Gadget"><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Select a content for the parameters</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-input-container class=md-dialog-content><label>Datasource</label><md-select required md-autofocus placeholder="Select new templace datasource" ng-model=config.datasource md-on-open=loadDatasources() ng-change=loadDatasourcesFields()><md-option ng-value={name:datasource.identification,refresh:datasource.refresh,type:datasource.mode,id:datasource.id} ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><div flex=""><md-content><md-list class=md-dense flex=""><md-list-item class=md-3-line ng-repeat="item in parameters"><div class=md-list-item-text layout=column><span>{{ item.label }}</span><md-input-container ng-if="item.type==\'labelsText\'" class=md-dialog-content><p>string value :</p><input type=text ng-model=item.value></md-input-container><md-input-container ng-if="item.type==\'labelsNumber\'" class=md-dialog-content><p>number value :</p><input type=number ng-model=item.value></md-input-container><md-input-container ng-if="item.type==\'labelsds\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter from datasource" ng-model=item.value><md-option ng-value={field:datasourceField.field,type:datasourceField.type} ng-repeat="datasourceField in datasourceFields">{{datasourceField.field}}</md-option></md-select></md-input-container><md-input-container ng-if="item.type==\'labelsdspropertie\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter from datasource" ng-model=item.value><md-option ng-value={field:datasourceField.field,type:datasourceField.type} ng-repeat="datasourceField in datasourceFields">{{datasourceField.field}}</md-option></md-select></md-input-container><md-input-container ng-if="item.type==\'selects\'" class=md-dialog-content><p>value :</p><md-select required md-autofocus placeholder="Select parameter value" ng-model=item.value><md-option ng-value=optionsValue ng-repeat="optionsValue in item.optionsValue">{{optionsValue}}</md-option></md-select></md-input-container></div></md-list-item></md-list></md-content></div><md-dialog-actions layout=row><span flex></span><md-button ng-click=save()>Ok</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/addWidgetBottomSheet.html','<md-bottom-sheet class=md-grid layout=column><div layout=row layout-align="center center" ng-cloak><h5>Drag&Drop your gadget to the grid panel</h5></div><div ng-cloak><div layout=row layout-align="center center" layout-wrap><div flex=12><md-button id=line draggable><md-icon>show_chart</md-icon><div class=md-grid-text>Line Chart</div></md-button></div><div flex=12><md-button id=bar draggable><md-icon>insert_chart</md-icon><div class=md-grid-text>Bar Chart</div></md-button></div><div flex=12><md-button id=pie draggable><md-icon>pie_chart</md-icon><div class=md-grid-text>Pie Chart</div></md-button></div><div flex=12><md-button id=wordcloud draggable><md-icon>sort_by_alpha</md-icon><div class=md-grid-text>Wordcloud</div></md-button></div><div flex=12><md-button id=map draggable><md-icon>map</md-icon><div class=md-grid-text>Map</div></md-button></div><div flex=12><md-button id=radar draggable><md-icon>track_changes</md-icon><div class=md-grid-text>Radar</div></md-button></div><div flex=12><md-button id=livehtml draggable><md-icon>art_track</md-icon><div class=md-grid-text>Live HTML</div></md-button></div><div flex=12><md-button id=table draggable><md-icon>view_list</md-icon><div class=md-grid-text>Table</div></md-button></div></div></div></md-bottom-sheet>');
$templateCache.put('app/partials/edit/datalinkDialog.html','<md-dialog aria-label=Pages><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Datalink</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Connections</md-subheader><md-table-container><table md-table ng-model=connections md-progress=promise><thead md-head><tr md-row><th md-column><span>Source Gadget</span></th><th md-column><span>Source Field</span></th><th md-column><span>Target Gadget</span></th><th md-column><span>Target Field</span></th><th md-column><span>Options</span></th></tr></thead><tbody md-body><tr md-row md-select=c md-select-id=name md-auto-select ng-repeat="c in connections"><td md-cell>{{ generateGadgetInfo(c.source) }}</td><td md-cell>{{c.sourceField}}</td><td md-cell>{{ generateGadgetInfo(c.target) }}</td><td md-cell>{{c.targetField}}</td><td md-cell><md-button class="md-icon-button md-warn" aria-label="Delete connection" ng-click=delete(c.source,c.sourceField,c.target,c.targetField)><md-icon>remove_circle</md-icon></md-button></td></tr></tbody></table></md-table-container><md-subheader>Add New Connection</md-subheader><md-list><md-list-item><md-input-container flex=25><label>Source Gadget</label><md-select ng-model=emitterGadget aria-label="Source Gadget" placeholder="Source Gadget" class=flex ng-change=refreshGadgetEmitterFields(emitterGadget)><md-option ng-repeat="gadget in gadgetsSources" ng-value=gadget.id>{{prettyGadgetInfo(gadget)}}</md-option></md-select></md-input-container><md-input-container flex=25><label>{{emitterDatasource?\'Source Field\' + \'(\' + emitterDatasource + \')\':\'Source Field\'}}</label><md-select ng-model=emitterGadgetField aria-label="Source Field" placeholder="{{emitterDatasource?\'Source Field\' + \'(\' + emitterDatasource + \')\':\'Source Field\'}}" class=flex><md-option ng-repeat="field in gadgetEmitterFields" ng-value=field.field>{{field.field}}</md-option></md-select></md-input-container><md-input-container flex=25><label>Target Gadget</label><md-select ng-model=targetGadget aria-label="Target Gadget" placeholder="Target Gadget" class=flex ng-change=refreshGadgetTargetFields(targetGadget)><md-option ng-repeat="gadget in gadgetsTargets" ng-value=gadget.id>{{prettyGadgetInfo(gadget)}}</md-option></md-select></md-input-container><md-input-container flex=25><label>{{targetDatasource?\'Target Field\' + \'(\' + targetDatasource + \')\':\'Target Field\'}}</label><md-select ng-model=targetGadgetField aria-label="Target Field" placeholder="{{targetDatasource?\'Target Field\' + \'(\' + targetDatasource + \')\':\'Target Field\'}}" class=flex><md-option ng-repeat="field in gadgetTargetFields" ng-value=field.field>{{field.field}}</md-option></md-select></md-input-container><md-input-container flex=5><md-button class="md-icon-button md-primary" aria-label="Add Connection" ng-click=create(emitterGadget,emitterGadgetField,targetGadget,targetGadgetField)><md-icon>add_circle</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/datasourcesDialog.html','<md-dialog aria-label=Layers><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Page Datasources</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Datasources</md-subheader><md-list><md-list-item ng-repeat="(nameDatasource, data) in dashboard.pages[selectedpage].datasources"><md-input-container flex=60><label>Datasource name</label><input ng-model=nameDatasource md-autofocus disabled></md-input-container><md-input-container flex=40><md-button ng-if="data.triggers.length == 0" class="md-icon-button md-warn" aria-label="Delete Datasource" ng-click=delete(nameDatasource)><md-icon>remove_circle</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Datasource</md-subheader><md-list><md-list-item><md-input-container flex=80><md-select required md-autofocus placeholder="Select new page datasource" ng-model=datasource md-on-open=loadDatasources()><md-option ng-if=!dashboard.pages[selectedpage].datasources[datasource.identification] ng-value=datasource ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><md-input-container flex=30><md-button class="md-icon-button md-primary" aria-label="Add Datasource" ng-click=create()><md-icon>add_circle</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editContainerDialog.html','<md-dialog aria-label=Container><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Edit Container</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Header</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=20><input ng-model=element.header.height type=number placeholder="Header Height"></md-input-container><md-checkbox flex=40 ng-model=element.header.enable class=flex>Enable widget header</md-checkbox><md-input-container flex=30><label>Background Color</label><color-picker flex=40 ng-model=element.header.backgroundColor></color-picker></md-input-container></div><md-subheader>Title</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=50><label>Widget Title</label><input ng-model=element.header.title.text required md-autofocus></md-input-container><md-input-container flex=30><label>Text Color</label><color-picker flex=50 ng-model=element.header.title.textColor></color-picker></md-input-container></div><md-subheader>Icon</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-autocomplete flex=50 ng-disabled=false md-no-cache=false md-selected-item=ctrl.icons[$index] md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=element.header.title.icon md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(element.header.title.icon)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of widget"><md-item-template><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><md-input-container flex=30><label>Icon Color</label><color-picker flex=50 ng-model=element.header.title.iconColor></color-picker></md-input-container></div><md-subheader>Body</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=30><label>Body Background</label><color-picker flex=100 ng-model=element.backgroundColor></color-picker></md-input-container><md-input-container flex=50><input ng-model=element.padding type=number placeholder="Content Padding"></md-input-container></div><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=33><input ng-model=element.border.width type=number placeholder="Border width"></md-input-container><md-input-container flex=33><input ng-model=element.border.radius type=number placeholder="Corner Radius"></md-input-container><md-input-container flex=30><label>Border Color</label><color-picker flex=33 ng-model=element.border.color></color-picker></md-input-container></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardButtons.html','<div class=toolbarButtons layout=row layout-align="right right" style=z-index:9000><span flex></span><md-button class="md-fab md-mini md-warn transparent-color" ng-click=ed.showListBottomSheet() aria-label="Add Element"><md-tooltip md-direction=bottom>Add Element</md-tooltip><md-icon>add</md-icon></md-button><md-button class="md-fab md-mini md-warn transparent-color" ng-click=ed.showDatalink() aria-label="Show datalink"><md-tooltip md-direction=bottom>Datalink</md-tooltip><md-icon>compare_arrows</md-icon></md-button><md-menu><md-button aria-label="Open menu with custom trigger" class="md-fab md-warn md-mini transparent-color" ng-click=$mdMenu.open()><md-icon>dashboard</md-icon></md-button><md-menu-content width=2><md-menu-item><md-button aria-label=Pages ng-click=ed.pagesEdit()><md-icon aria-label=Pages>collections</md-icon>Pages</md-button></md-menu-item><md-menu-item><md-button aria-label="Configure Dashboard" ng-click=ed.dashboardEdit()><md-icon aria-label="Configure Dashboard">settings</md-icon>Configure Dashboard</md-button></md-menu-item><md-menu-item><md-button aria-label="Dashboard Style" ng-click=ed.dashboardStyleEdit()><md-icon aria-label="Configure Dashboard">format_paint</md-icon>Dashboard Style</md-button></md-menu-item></md-menu-content></md-menu><md-button class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.savePage() aria-label="Save Dashboard"><md-tooltip md-direction=bottom>Save Dashboard</md-tooltip><md-icon>save</md-icon></md-button><md-button class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.deleteDashboard() aria-label="Delete Dashboard"><md-tooltip md-direction=bottom>Delete Dashboard</md-tooltip><md-icon>delete</md-icon></md-button><md-button class="md-fab md-primary md-mini md-hue-2 transparent-color" ng-click=ed.closeDashboard() aria-label="Close Dashboard Editor"><md-tooltip md-direction=bottom>Close Dashboard Editor</md-tooltip><md-icon>close</md-icon></md-button></div>');
$templateCache.put('app/partials/edit/editDashboardDialog.html','<md-dialog aria-label=Layers><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Dashboard configuration</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Header</md-subheader><md-input-container flex=70><label>Title</label><input ng-model=dashboard.header.title md-autofocus></md-input-container><md-checkbox flex=40 ng-model=dashboard.header.enable class=flex>Enable header</md-checkbox><md-input-container flex=30><label>Header Color</label><color-picker options="{restrictToFormat:false, preserveInputFormat:false}" ng-model=dashboard.header.backgroundColor></color-picker></md-input-container><md-input-container flex=30><label>Title Color</label><color-picker ng-model=dashboard.header.textColor></color-picker></md-input-container><md-input-container flex=30><label>Icon Color</label><color-picker ng-model=dashboard.header.iconColor></color-picker></md-input-container><md-input-container flex=30><label>Page Color</label><color-picker ng-model=dashboard.header.pageColor></color-picker></md-input-container><md-input-container flex=50><input ng-model=dashboard.header.height min=0 max=200 step=1 type=number placeholder="Header Height"></md-input-container><md-input-container flex=50><input ng-model=dashboard.header.logo.height min=0 max=200 step=1 type=number placeholder="Logo Height"></md-input-container><lf-ng-md-file-input flex=70 ng-change=onFilesChange() lf-api=apiUpload lf-files=auxUpload.file lf-placeholder="" lf-browse-label="Change Logo Img" accept=image/* progress lf-filesize=1MB lf-remove-label=""></lf-ng-md-file-input><md-subheader>Visibility</md-subheader><md-checkbox ng-model=dashboard.public class=flex>Public Dashboard</md-checkbox><md-subheader>Navigation</md-subheader><md-checkbox ng-model=dashboard.navigation.showBreadcrumb class=flex>Show Breadcrumbs</md-checkbox><md-checkbox ng-model=dashboard.navigation.showBreadcrumbIcon class=flex>Show Breadcrumbs Icon</md-checkbox><md-subheader>Grid Settings</md-subheader><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=dashboard.gridOptions.gridType ng-change=changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=dashboard.gridOptions.compactType ng-change=changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.minCols type=number placeholder="Min Grid Cols" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.maxCols type=number placeholder="Max Grid Cols" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.minRows type=number placeholder="Min Grid Rows" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.maxRows type=number placeholder="Max Grid Rows" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.margin min=0 max=100 step=1 type=number placeholder=Margin ng-change=changedOptions()></md-input-container><md-checkbox ng-model=dashboard.gridOptions.outerMargin ng-change=changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=changedOptions()></md-input-container><md-checkbox ng-model=dashboard.gridOptions.disableWindowResize ng-change=changedOptions() class=flex>Disable window resize</md-checkbox></div><md-subheader>Item Settings</md-subheader><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.defaultItemRows type=number placeholder="Default Item Rows" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.defaultItemCols type=number placeholder="Default Item Cols" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=dashboard.gridOptions.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=changedOptions()></md-input-container><md-input-container class=flex><input ng-model=dashboard.gridOptions.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=dashboard.gridOptions.keepFixedHeightInMobile ng-change=changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=dashboard.gridOptions.keepFixedWidthInMobile ng-change=changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editDashboardSidenav.html','<md-sidenav class="site-sidenav md-sidenav-left md-whiteframe-4dp layout-padding" md-component-id=left md-is-locked-open=false><label class=md-headline>Grid Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><md-input-container class=md-block flex-gt-sm><label>Dashboard Title</label><input ng-model=title></md-input-container></md-input-container><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=main.options.gridType ng-change=main.changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=main.options.compactType ng-change=main.changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.swap ng-change=main.changedOptions() class=flex>Swap Items</md-checkbox><md-checkbox ng-model=main.options.pushItems ng-change=main.changedOptions() class=flex>Push Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.disablePushOnDrag ng-change=main.changedOptions() class=flex>Disable Push On Drag</md-checkbox><md-checkbox ng-model=main.options.disablePushOnResize ng-change=main.changedOptions() class=flex>Disable Push On Resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.north ng-change=main.changedOptions() class=flex>Push North</md-checkbox><md-checkbox ng-model=main.options.pushDirections.east ng-change=main.changedOptions() class=flex>Push East</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.south ng-change=main.changedOptions() class=flex>Push South</md-checkbox><md-checkbox ng-model=main.options.pushDirections.west ng-change=main.changedOptions() class=flex>Push West</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.draggable.enabled ng-change=main.changedOptions() class=flex>Drag Items</md-checkbox><md-checkbox ng-model=main.options.resizable.enabled ng-change=main.changedOptions() class=flex>Resize Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushResizeItems ng-change=main.changedOptions() class=flex>Push Resize Items</md-checkbox><md-input-container class=flex><label>Display grid lines</label><md-select aria-label="Display grid lines" ng-model=main.options.displayGrid placeholder="Display grid lines" ng-change=main.changedOptions()><md-option value=always>Always</md-option><md-option value=onDrag&Resize>On Drag & Resize</md-option><md-option value=none>None</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minCols type=number placeholder="Min Grid Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxCols type=number placeholder="Max Grid Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minRows type=number placeholder="Min Grid Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxRows type=number placeholder="Max Grid Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.margin min=0 max=30 step=1 type=number placeholder=Margin ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.outerMargin ng-change=main.changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.disableWindowResize ng-change=main.changedOptions() class=flex>Disable window resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.scrollToNewItems ng-change=main.changedOptions() class=flex>Scroll to new items</md-checkbox><md-checkbox ng-model=main.options.disableWarnings ng-change=main.changedOptions() class=flex>Disable console warnings</md-checkbox></div><label class=md-headline>Item Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemCols type=number placeholder="Max Item Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemCols type=number placeholder="Min Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemRows type=number placeholder="Max Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemRows type=number placeholder="Min Item Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemArea type=number placeholder="Max Item Area" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemArea type=number placeholder="Min Item Area" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.defaultItemRows type=number placeholder="Default Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.defaultItemCols type=number placeholder="Default Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.keepFixedHeightInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=main.options.keepFixedWidthInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellClick ng-change=main.changedOptions() class=flex>Enable click to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellContextMenu ng-change=main.changedOptions() class=flex>Enable right click to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellDrop ng-change=main.changedOptions() class=flex>Enable drop to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellDrag ng-change=main.changedOptions() class=flex>Enable drag to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxCols type=number placeholder="Drag Max Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxRows type=number placeholder="Drag Max Rows" ng-change=main.changedOptions()></md-input-container></div></md-sidenav>');
$templateCache.put('app/partials/edit/editDashboardStyleDialog.html','<md-dialog aria-label=Layers><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Dashboard configuration</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Widget Header</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=20><input ng-model=style.header.height type=number placeholder="Header Height"></md-input-container><md-checkbox flex=30 ng-model=style.header.enable class=flex>Enable widget header</md-checkbox><md-input-container flex=30><label>Header Background</label><color-picker flex=40 ng-model=style.header.backgroundColor></color-picker></md-input-container></div><md-subheader>Widgets Title</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=30><label>Header Text Color</label><color-picker flex=50 ng-model=style.header.title.textColor></color-picker></md-input-container><md-input-container flex=30><label>Header Icon Color</label><color-picker flex=50 ng-model=style.header.title.iconColor></color-picker></md-input-container></div><md-subheader>Widget Body</md-subheader><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=30><label>Body Background</label><color-picker flex=100 ng-model=style.backgroundColor></color-picker></md-input-container><md-input-container flex=50><input ng-model=style.padding type=number placeholder="Content Padding"></md-input-container></div><div class="md-dialog-content layout-row layout-align-start-center flex"><md-input-container flex=33><input ng-model=style.border.width type=number placeholder="Border width"></md-input-container><md-input-container flex=33><input ng-model=style.border.radius type=number placeholder="Corner Radius"></md-input-container><md-input-container flex=30><label>Border Color</label><color-picker flex=33 ng-model=style.border.color></color-picker></md-input-container></div></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editGadgetDialog.html','<md-dialog aria-label=GadgetEditor><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Edit Gadget</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-subheader>Edit Live Content</md-subheader><md-input-container class=md-dialog-content><label>Datasource</label><md-select required md-autofocus placeholder="Select new templace datasource" ng-model=element.datasource md-on-open=loadDatasources()><md-option ng-value={name:datasource.identification,id:datasource.id,refresh:datasource.refresh,type:datasource.mode} ng-repeat="datasource in datasources">{{datasource.identification}}</md-option></md-select></md-input-container><md-input-container class=md-dialog-content><div style=width:500px md-autofocus ui-codemirror="{lineWrapping: true, fixedGutter: false, lineNumbers: true, theme:\'twilight\', lineWrapping : true, mode: \'xml\', autofocus: true}" ng-model=element.content></div></md-input-container><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/editPageButtons.html','<div class=sidenav-fab layout=row layout-align="center end"><md-button class="md-fab md-mini md-primary" ng-click=main.sidenav.toggle()><md-icon>settings</md-icon></md-button><md-button class="md-fab md-mini md-danger" ng-click=main.addItem()><md-icon>add</md-icon><md-tooltip>Add widget</md-tooltip></md-button></div>');
$templateCache.put('app/partials/edit/editPageSidenav.html','<md-sidenav class="site-sidenav md-sidenav-left md-whiteframe-4dp layout-padding" md-component-id=left md-is-locked-open=true><label class=md-headline>Grid Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><md-input-container class=md-block flex-gt-sm><label>Dashboard Title</label><input ng-model=title></md-input-container></md-input-container><md-input-container class=flex><label>Grid Type</label><md-select aria-label="Grid type" ng-model=main.options.gridType ng-change=main.changedOptions() placeholder="Grid Type" class=flex><md-option value=fit>Fit to screen</md-option><md-option value=scrollVertical>Scroll Vertical</md-option><md-option value=scrollHorizontal>Scroll Horizontal</md-option><md-option value=fixed>Fixed</md-option><md-option value=verticalFixed>Vertical Fixed</md-option><md-option value=horizontalFixed>Horizontal Fixed</md-option></md-select></md-input-container><md-input-container class=flex><label>Compact Type</label><md-select aria-label="Compact type" ng-model=main.options.compactType ng-change=main.changedOptions() placeholder="Compact Type" class=flex><md-option value=none>None</md-option><md-option value=compactUp>Compact Up</md-option><md-option value=compactLeft>Compact Left</md-option><md-option value=compactLeft&Up>Compact Left & Up</md-option><md-option value=compactUp&Left>Compact Up & Left</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.swap ng-change=main.changedOptions() class=flex>Swap Items</md-checkbox><md-checkbox ng-model=main.options.pushItems ng-change=main.changedOptions() class=flex>Push Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.disablePushOnDrag ng-change=main.changedOptions() class=flex>Disable Push On Drag</md-checkbox><md-checkbox ng-model=main.options.disablePushOnResize ng-change=main.changedOptions() class=flex>Disable Push On Resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.north ng-change=main.changedOptions() class=flex>Push North</md-checkbox><md-checkbox ng-model=main.options.pushDirections.east ng-change=main.changedOptions() class=flex>Push East</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushDirections.south ng-change=main.changedOptions() class=flex>Push South</md-checkbox><md-checkbox ng-model=main.options.pushDirections.west ng-change=main.changedOptions() class=flex>Push West</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.draggable.enabled ng-change=main.changedOptions() class=flex>Drag Items</md-checkbox><md-checkbox ng-model=main.options.resizable.enabled ng-change=main.changedOptions() class=flex>Resize Items</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.pushResizeItems ng-change=main.changedOptions() class=flex>Push Resize Items</md-checkbox><md-input-container class=flex><label>Display grid lines</label><md-select aria-label="Display grid lines" ng-model=main.options.displayGrid placeholder="Display grid lines" ng-change=main.changedOptions()><md-option value=always>Always</md-option><md-option value=onDrag&Resize>On Drag & Resize</md-option><md-option value=none>None</md-option></md-select></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minCols type=number placeholder="Min Grid Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxCols type=number placeholder="Max Grid Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.minRows type=number placeholder="Min Grid Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.maxRows type=number placeholder="Max Grid Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.margin min=0 max=30 step=1 type=number placeholder=Margin ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.outerMargin ng-change=main.changedOptions() class=flex>Outer Margin</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.mobileBreakpoint type=number placeholder="Mobile Breakpoint" ng-change=main.changedOptions()></md-input-container><md-checkbox ng-model=main.options.disableWindowResize ng-change=main.changedOptions() class=flex>Disable window resize</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.scrollToNewItems ng-change=main.changedOptions() class=flex>Scroll to new items</md-checkbox><md-checkbox ng-model=main.options.disableWarnings ng-change=main.changedOptions() class=flex>Disable console warnings</md-checkbox></div><label class=md-headline>Item Settings</label><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemCols type=number placeholder="Max Item Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemCols type=number placeholder="Min Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemRows type=number placeholder="Max Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemRows type=number placeholder="Min Item Rows" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.maxItemArea type=number placeholder="Max Item Area" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.minItemArea type=number placeholder="Min Item Area" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.defaultItemRows type=number placeholder="Default Item Rows" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.defaultItemCols type=number placeholder="Default Item Cols" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.fixedColWidth type=number placeholder="Fixed Col Width" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.fixedRowHeight type=number placeholder="Fixed layout-row layout-align-start-center Height" ng-change=main.changedOptions()></md-input-container></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.keepFixedHeightInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Height In Mobile</md-checkbox><md-checkbox ng-model=main.options.keepFixedWidthInMobile ng-change=main.changedOptions() class=flex>Keep Fixed Width In Mobile</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellClick ng-change=main.changedOptions() class=flex>Enable click to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellContextMenu ng-change=main.changedOptions() class=flex>Enable right click to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-checkbox ng-model=main.options.enableEmptyCellDrop ng-change=main.changedOptions() class=flex>Enable drop to add</md-checkbox><md-checkbox ng-model=main.options.enableEmptyCellDrag ng-change=main.changedOptions() class=flex>Enable drag to add</md-checkbox></div><div class="layout-row layout-align-start-center flex"><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxCols type=number placeholder="Drag Max Cols" ng-change=main.changedOptions()></md-input-container><md-input-container class=flex><input ng-model=main.options.emptyCellDragMaxRows type=number placeholder="Drag Max Rows" ng-change=main.changedOptions()></md-input-container></div></md-sidenav>');
$templateCache.put('app/partials/edit/layersDialog.html','<md-dialog aria-label=Layers><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Page Layers</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Layers</md-subheader><md-list><md-list-item ng-repeat="layer in dashboard.pages[selectedpage].layers"><md-input-container flex=70><label>Layer name</label><input ng-model=layer.title required md-autofocus></md-input-container><md-input-container flex=30><md-button ng-if="!$first && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=up ng-click=moveUpLayer($index)><md-icon>arrow_upward</md-icon></md-button><md-button ng-if="!$last && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=down ng-click=moveDownLayer($index)><md-icon>arrow_downward</md-icon></md-button><md-button ng-if="dashboard.pages[selectedpage].layers.length > 1" class="md-icon-button md-warn" aria-label="Delete layer" ng-click=delete($index)><md-icon>remove_circle</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Layer</md-subheader><md-list><md-list-item><md-input-container flex=70><label>Layer name</label><input ng-model=title required md-autofocus></md-input-container><md-input-container flex=30><md-button class="md-icon-button md-primary" aria-label="Add layer" ng-click=create()><md-icon>add_circle</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/edit/pagesDialog.html','<md-dialog aria-label=Pages><form ng-cloak><md-toolbar><div class=md-toolbar-tools><h2>Dashboard Pages</h2><span flex></span><md-button class=md-icon-button ng-click=cancel()><b>X</b></md-button></div></md-toolbar><md-dialog-content><md-subheader>Pages</md-subheader><md-list><md-list-item ng-repeat="page in dashboard.pages"><md-input-container flex=35><label>Page name</label><input ng-model=page.title required md-autofocus></md-input-container><md-autocomplete flex=35 ng-disabled=false md-no-cache=false md-selected-item=ctrl.icons[$index] md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=page.icon md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(page.icon)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of page"><md-item-template><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><md-input-container flex=35><label>Background Color</label><color-picker options="{restrictToFormat:false, preserveInputFormat:false}" ng-model=page.background.color></color-picker></md-input-container><lf-ng-md-file-input ng-change=onFilesChange($index) lf-api=apiUpload[$index] lf-files=auxUpload[$index].file lf-placeholder="" lf-browse-label="Change Background Img" accept=image/* progress lf-filesize=5MB lf-remove-label=""></lf-ng-md-file-input><md-input-container flex=30><md-button ng-if="!$first && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=up ng-click=moveUpPage($index)><md-icon>arrow_upward</md-icon></md-button><md-button ng-if="!$last && dashboard.pages.length > 1" class="md-icon-button md-primary" aria-label=down ng-click=moveDownPage($index)><md-icon>arrow_downward</md-icon></md-button><md-button ng-if="dashboard.pages.length > 1" class="md-icon-button md-warn" aria-label="Delete page" ng-click=delete($index)><md-icon>remove_circle</md-icon></md-button></md-input-container></md-list-item></md-list><md-subheader>Add New Page</md-subheader><md-list><md-list-item><md-input-container flex=35><label>Page name</label><input ng-model=title required md-autofocus></md-input-container><md-autocomplete flex=35 ng-disabled=false md-no-cache=false md-selected-item=selectedIconItem md-search-text-change=ctrl.searchTextChange(ctrl.searchText) md-search-text=searchIconText md-selected-item-change=ctrl.selectedItemChange(item) md-items="icon in queryIcon(searchIconText)" md-item-text=icon md-min-length=0 md-menu-class=autocomplete-custom-template md-floating-label="Select icon of page"><md-item-template><span class=item-title><md-icon>{{icon}}</md-icon><span>{{icon}}</span></span></md-item-template></md-autocomplete><lf-ng-md-file-input lf-files=file lf-placeholder="" lf-browse-label="Change Background Img" accept=image/* progress lf-filesize=5MB lf-remove-label=""></lf-ng-md-file-input><md-input-container flex=30><md-button class="md-icon-button md-primary" aria-label="Add page" ng-click=create()><md-icon>add_circle</md-icon></md-button></md-input-container></md-list-item></md-list></md-dialog-content><md-dialog-actions layout=row><span flex></span><md-button ng-click=hide() class=md-primary>Close</md-button></md-dialog-actions></form></md-dialog>');
$templateCache.put('app/partials/view/filterTooltip.html','<b>Applied filters:</b> <span class=no-wrap ng-repeat="(field, data) in vm.datastatus"><br><label>{{field}}</label>:<label md-truncate>{{vm.generateFilterInfo(data)}}</label><md-button class="md-icon-button md-warn" aria-label="Delete filter" ng-click=vm.deleteFilter(data.id,field)><md-icon>delete</md-icon></md-button></span>');
$templateCache.put('app/partials/view/header.html','<md-toolbar ng-if=vm.dashboard.header.enable layout=row class=md-hue-2 layout-align="space-between center" ng-style="{\'height\': + vm.dashboard.header.height + \'px\', \'background\': vm.dashboard.header.backgroundColor}"><md-headline layout=row layout-align="start center" class=left-margin-10><img ng-if=vm.dashboard.header.logo.filedata ng-src={{vm.dashboard.header.logo.filedata}} ng-style="{\'height\': + vm.dashboard.header.logo.height + \'px\'}"><span ng-style="{\'color\': vm.dashboard.header.textColor}">{{\'&nbsp;\' + vm.dashboard.header.title}}</span><md-icon ng-style="{\'color\': vm.dashboard.header.iconColor}" ng-if=vm.dashboard.navigation.showBreadcrumbIcon>keyboard_arrow_right</md-icon><span ng-style="{\'color\': vm.dashboard.header.pageColor}" ng-if=vm.dashboard.navigation.showBreadcrumb>{{vm.dashboard.pages[vm.selectedpage].title}}</span></md-headline><md-button class="md-mini md-icon-button" aria-label="Open Menu" ng-click=vm.sidenav.toggle();><md-tooltip md-direction=left>Toggle Menu</md-tooltip><md-icon>reorder</md-icon></md-button></md-toolbar>');
$templateCache.put('app/partials/view/sidenav.html','<md-sidenav class="md-sidenav-left md-whiteframe-4dp" md-component-id=left><header class=nav-header></header><md-content flex="" role=navigation class="_md flex"><md-subheader class=md-no-sticky>Pages</md-subheader><md-list class=md-hue-2><span ng-repeat="page in vm.dashboard.pages"><md-list-item md-colors="{background: ($index===vm.selectedpage ? \'primary\' : \'grey-A100\')}" ng-click=vm.setIndex($index) flex><md-icon>{{page.icon}}</md-icon><p>{{page.title}}</p></md-list-item></span></md-list></md-content></md-sidenav>');
$templateCache.put('app/components/view/pageComponent/page.html','<div class=page-dashboard-container ng-style="{\'background-image\':\'url(\' + vm.page.background.filedata + \')\',\'background-color\': vm.page.background.color }"><span ng-repeat="layer in vm.page.layers"><gridster ng-style="vm.dashboardheader.enable && {\'height\': \'calc(100% - \'+{{vm.dashboardheader.height}}+\'px\'+\')\'} || {\'height\': \'100%\'}" ng-if="(vm.page.combinelayers || vm.page.selectedlayer == $index) " options=vm.gridoptions class=flex><element ng-style="{\'z-index\':$parent.$index*500+1}" ng-if=item.id element=item editmode=vm.editmode ng-repeat="item in layer.gridboard"></element></gridster></span></div>');
$templateCache.put('app/components/view/elementComponent/element.html','<gridster-item item=vm.element ng-style="{ \'border-width\': vm.element.border.width + \'px\', \'border-color\': vm.element.border.color, \'border-radius\': vm.element.border.radius + \'px\', \'border-style\': \'solid\'}"><div class="element-container fullcontainer"><md-toolbar ng-if=vm.element.header.enable class="widget-header md-hue-2" ng-style="{\'background\':vm.element.header.backgroundColor, \'height\': vm.element.header.height + \'px\'}"><div class=md-toolbar-tools><md-icon ng-style="{\'color\':vm.element.header.title.iconColor,\'font-size\' : \'20px\'}">{{vm.element.header.title.icon}}</md-icon><h5 flex ng-style="{\'color\':vm.element.header.title.textColor}" md-truncate>{{vm.element.header.title.text}}</h5><md-icon class=cursor-hand style="font-size: 20px;color:hsl(220, 23%, 20%);" ng-if=vm.datastatus tooltips tooltip-show-trigger=click tooltip-hide-trigger=click tooltip-close-button=true tooltip-size=small tooltip-template-url=app/partials/view/filterTooltip.html ng-attr-tooltip-side="{{vm.editmode?\'bottom\':\'bottom left\'}}">filter_list</md-icon><md-button ng-if=vm.editmode ng-click=vm.openEditContainerDialog() class=md-icon-button aria-label="Edit Container"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">format_paint</md-icon><md-tooltip>Edit container</md-tooltip></md-button><md-button ng-if="vm.editmode && vm.element.type == \'livehtml\'" ng-click=vm.openEditGadgetDialog() class=md-icon-button aria-label="Gadget Editor"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">mode_edit</md-icon><md-tooltip>Edit Gadget definition</md-tooltip></md-button><md-button ng-if=vm.editmode class="drag-handler md-icon-button"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">open_with</md-icon><md-tooltip>Move</md-tooltip></md-button><md-button ng-if=vm.editmode class="remove-button md-icon-button" ng-click=vm.deleteElement()><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">delete</md-icon><md-tooltip>Remove</md-tooltip></md-button></div></md-toolbar><div ng-if="vm.editmode && !vm.element.header.enable" class=item-buttons><md-button ng-click=vm.openEditContainerDialog() class="md-raised md-icon-button" aria-label="Edit Container"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">format_paint</md-icon></md-button><md-button ng-click=vm.openEditGadgetDialog() ng-if="vm.element.type == \'livehtml\'" class="md-raised md-icon-button" aria-label="Gadget Editor"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">mode_edit</md-icon></md-button><md-button ng-if=vm.editmode class="drag-handler md-raised md-icon-button"><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">open_with</md-icon></md-button><md-button ng-if=vm.editmode class="remove-button md-raised md-icon-button" ng-click=vm.deleteElement()><md-icon style="font-size: 20px;color:hsl(220, 23%, 20%);">delete</md-icon><md-tooltip>Remove</md-tooltip></md-button></div><livehtml ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\', \'height\': vm.calcHeight()}" ng-if="vm.element.type == \'livehtml\'" livecontent=vm.element.content datasource=vm.element.datasource ng-class=vm.element.id id=vm.element.id datastatus=vm.datastatus></livehtml><gadget ng-style="{\'background-color\':vm.element.backgroundColor, \'padding\': vm.element.padding + \'px\', \'display\': \'inline-block\', \'width\':\'100%\', \'height\': vm.calcHeight()}" ng-if="vm.element.type != \'livehtml\'" ng-class=vm.element.id id=vm.element.id datastatus=vm.datastatus></gadget></div></gridster-item>');
$templateCache.put('app/components/view/gadgetComponent/gadget.html','<div class=spinner-margin-top ng-if="vm.type == \'loading\'" layout=row layout-sm=column layout-align=space-around><md-progress-circular md-diameter=60></md-progress-circular></div><div class=spinner-overlay ng-if="vm.status == \'pending\'" layout=row layout-sm=column layout-align=space-around><md-progress-linear md-mode=indeterminate></md-progress-linear></div><div ng-if="vm.type == \'nodata\'" layout=row layout-sm=column layout-align=space-around><h3>No data found</h3></div><canvas ng-if="vm.type == \'line\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-line" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'bar\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-bar" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'pie\'" chart-click=vm.clickChartEventProcessorEmitter class="chart chart-doughnut" chart-data=vm.data chart-labels=vm.labels chart-options=vm.optionsChart></canvas><canvas ng-if="vm.type == \'radar\'" chart-dataset-override=vm.datasetOverride chart-click=vm.clickChartEventProcessorEmitter class="chart chart-radar" chart-data=vm.data chart-labels=vm.labels chart-series=vm.series chart-options=vm.optionsChart></canvas><word-cloud ng-if="vm.type == \'wordcloud\'" words=vm.words width=vm.width height=vm.height padding=0 use-tooltip=false use-transition=true></word-cloud><leaflet id="{{\'lmap\' + vm.id}}" ng-if="vm.type == \'map\'" lf-center=vm.center markers=vm.markers height={{vm.height}} width=100%></leaflet><md-table-container ng-style="{\'height\': \'calc(100% - \'+{{vm.config.config.tablePagination.style.trHeightFooter}}+\'px\'+\')\'}" ng-if="vm.type == \'table\'"><table md-table md-progress=promise md-row-select=vm.config.config.tablePagination.options.rowSelection ng-model=vm.selected><thead md-head ng-if=!vm.config.config.tablePagination.options.decapitate ng-style="{\'background-color\':vm.config.config.tablePagination.style.backGroundTHead}" md-order=vm.config.config.tablePagination.order><tr md-row ng-style="{\'height\':vm.config.config.tablePagination.style.trHeightHead}"><th ng-style="{\'color\':vm.config.config.tablePagination.style.textColorTHead}" md-column ng-repeat="measure in vm.measures" md-order-by={{measure.config.order}}><span>{{measure.config.name}}</span></th></tr></thead><tbody md-body><tr md-row md-auto-select=true md-on-select=vm.selectItemTable md-select=dat ng-style="{\'height\':vm.config.config.tablePagination.style.trHeightBody}" ng-repeat="dat in vm.data | orderBy: vm.config.config.tablePagination.order |  limitTo: vm.config.config.tablePagination.limit : (vm.config.config.tablePagination.page -1) * vm.config.config.tablePagination.limit"><td ng-style="{\'color\':vm.config.config.tablePagination.style.textColorBody}" md-cell ng-repeat="value in dat">{{value}}</td></tr></tbody></table></md-table-container><md-table-pagination ng-if="vm.type == \'table\'" md-limit=vm.config.config.tablePagination.limit md-limit-options="vm.notSmall ? vm.config.config.tablePagination.limitOptions : undefined" md-page=vm.config.config.tablePagination.page md-total={{vm.data.length}} md-page-select="vm.config.config.tablePagination.options.pageSelect && vm.notSmall" md-boundary-links="vm.config.config.tablePagination.options.boundaryLinks && vm.notSmall" ng-style="{\'background-color\':vm.config.config.tablePagination.style.backGroundTFooter,\'height\':vm.config.config.tablePagination.style.trHeightFooter, \'color\':vm.config.config.tablePagination.style.textColorFooter}"></md-table-pagination>');
$templateCache.put('app/components/view/liveHTMLComponent/livehtml.html','<div id=testhtml>{{1+1}}</div>');
$templateCache.put('app/components/edit/editDashboardComponent/edit.dashboard.html','<ng-include src="\'app/partials/edit/editDashboardButtons.html\'"></ng-include><ng-include src="\'app/partials/edit/editDashboardSidenav.html\'"></ng-include>');}]);