package com.gdet.testapp.someip.car;

/**
 * 车载服务ID定义
 * 根据AUTOSAR规范定义各种车载服务的标识符
 * 
 * @author Android Team
 * @version 1.0
 */
public final class CarServices {
    
    // 私有构造函数，防止实例化
    private CarServices() {}
    
    // 车载服务ID定义
    public static final short HVAC_SERVICE_ID = (short) 0x1234;      // 空调控制服务
    public static final short DOOR_SERVICE_ID = (short) 0x1235;      // 车门控制服务
    public static final short ENGINE_SERVICE_ID = (short) 0x1236;    // 发动机信息服务
    public static final short NAVIGATION_SERVICE_ID = (short) 0x1237; // 导航服务
    public static final short MEDIA_SERVICE_ID = (short) 0x1238;     // 媒体控制服务
    
    /**
     * HVAC服务方法ID定义
     */
    public static final class HvacMethods {
        private HvacMethods() {}
        
        public static final short GET_TEMPERATURE = (short) 0x0001;
        public static final short SET_TEMPERATURE = (short) 0x0002;
        public static final short GET_FAN_SPEED = (short) 0x0003;
        public static final short SET_FAN_SPEED = (short) 0x0004;
        public static final short GET_AC_STATUS = (short) 0x0005;
        public static final short SET_AC_STATUS = (short) 0x0006;
    }
    
    /**
     * 车门服务方法ID定义
     */
    public static final class DoorMethods {
        private DoorMethods() {}
        
        public static final short GET_DOOR_STATUS = (short) 0x0001;
        public static final short LOCK_DOORS = (short) 0x0002;
        public static final short UNLOCK_DOORS = (short) 0x0003;
        public static final short GET_WINDOW_STATUS = (short) 0x0004;
        public static final short CONTROL_WINDOW = (short) 0x0005;
    }
    
    /**
     * 发动机服务方法ID定义
     */
    public static final class EngineMethods {
        private EngineMethods() {}
        
        public static final short GET_ENGINE_STATUS = (short) 0x0001;
        public static final short GET_RPM = (short) 0x0002;
        public static final short GET_SPEED = (short) 0x0003;
        public static final short GET_FUEL_LEVEL = (short) 0x0004;
        public static final short GET_ENGINE_TEMPERATURE = (short) 0x0005;
    }
    
    /**
     * 导航服务方法ID定义
     */
    public static final class NavigationMethods {
        private NavigationMethods() {}
        
        public static final short SET_DESTINATION = (short) 0x0001;
        public static final short GET_CURRENT_POSITION = (short) 0x0002;
        public static final short START_NAVIGATION = (short) 0x0003;
        public static final short STOP_NAVIGATION = (short) 0x0004;
        public static final short GET_ROUTE_INFO = (short) 0x0005;
    }
    
    /**
     * 媒体服务方法ID定义
     */
    public static final class MediaMethods {
        private MediaMethods() {}
        
        public static final short PLAY = (short) 0x0001;
        public static final short PAUSE = (short) 0x0002;
        public static final short STOP = (short) 0x0003;
        public static final short NEXT_TRACK = (short) 0x0004;
        public static final short PREVIOUS_TRACK = (short) 0x0005;
        public static final short SET_VOLUME = (short) 0x0006;
        public static final short GET_VOLUME = (short) 0x0007;
        public static final short GET_CURRENT_TRACK = (short) 0x0008;
    }
    
    /**
     * 事件组ID定义
     */
    public static final class EventGroups {
        private EventGroups() {}
        
        public static final short HVAC_EVENTS = (short) 0x0001;
        public static final short DOOR_EVENTS = (short) 0x0002;
        public static final short ENGINE_EVENTS = (short) 0x0003;
        public static final short NAVIGATION_EVENTS = (short) 0x0004;
        public static final short MEDIA_EVENTS = (short) 0x0005;
    }
    
    /**
     * 事件ID定义
     */
    public static final class Events {
        private Events() {}
        
        // HVAC事件
        public static final short TEMPERATURE_CHANGED = (short) 0x8001;
        public static final short FAN_SPEED_CHANGED = (short) 0x8002;
        public static final short AC_STATUS_CHANGED = (short) 0x8003;
        
        // 车门事件
        public static final short DOOR_STATUS_CHANGED = (short) 0x8004;
        public static final short WINDOW_STATUS_CHANGED = (short) 0x8005;
        
        // 发动机事件
        public static final short ENGINE_STATUS_CHANGED = (short) 0x8006;
        public static final short SPEED_CHANGED = (short) 0x8007;
        public static final short FUEL_LEVEL_CHANGED = (short) 0x8008;
        
        // 导航事件
        public static final short POSITION_UPDATED = (short) 0x8009;
        public static final short ROUTE_CALCULATED = (short) 0x800A;
        public static final short NAVIGATION_INSTRUCTION = (short) 0x800B;
        
        // 媒体事件
        public static final short TRACK_CHANGED = (short) 0x800C;
        public static final short VOLUME_CHANGED = (short) 0x800D;
        public static final short PLAYBACK_STATUS_CHANGED = (short) 0x800E;
    }
    
    /**
     * 网络配置常量
     */
    public static final class NetworkConfig {
        private NetworkConfig() {}
        
        public static final String MULTICAST_ADDRESS = "224.0.0.251";
        public static final int SERVICE_DISCOVERY_PORT = 30490;
        public static final int DEFAULT_SERVICE_PORT = 30501;
        public static final int CLIENT_PORT_START = 30502;
        public static final int CLIENT_PORT_END = 30600;
    }
    
    /**
     * 协议配置常量
     */
    public static final class ProtocolConfig {
        private ProtocolConfig() {}
        
        public static final int REQUEST_TIMEOUT_MS = 10000;
        public static final int SERVICE_DISCOVERY_TIMEOUT_MS = 5000;
        public static final int HEARTBEAT_INTERVAL_MS = 30000;
        public static final int MAX_MESSAGE_SIZE = 1400;
        public static final int MAX_PENDING_REQUESTS = 100;
    }
    
    /**
     * 获取服务名称
     * @param serviceId 服务ID
     * @return 服务名称
     */
    public static String getServiceName(short serviceId) {
        switch (serviceId) {
            case HVAC_SERVICE_ID: return "HVAC Control Service";
            case DOOR_SERVICE_ID: return "Door Control Service";
            case ENGINE_SERVICE_ID: return "Engine Information Service";
            case NAVIGATION_SERVICE_ID: return "Navigation Service";
            case MEDIA_SERVICE_ID: return "Media Control Service";
            default: return "Unknown Service";
        }
    }
    
    /**
     * 获取方法名称
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @return 方法名称
     */
    public static String getMethodName(short serviceId, short methodId) {
        switch (serviceId) {
            case HVAC_SERVICE_ID:
                return getHvacMethodName(methodId);
            case DOOR_SERVICE_ID:
                return getDoorMethodName(methodId);
            case ENGINE_SERVICE_ID:
                return getEngineMethodName(methodId);
            case NAVIGATION_SERVICE_ID:
                return getNavigationMethodName(methodId);
            case MEDIA_SERVICE_ID:
                return getMediaMethodName(methodId);
            default:
                return "Unknown Method";
        }
    }
    
    private static String getHvacMethodName(short methodId) {
        switch (methodId) {
            case HvacMethods.GET_TEMPERATURE: return "GetTemperature";
            case HvacMethods.SET_TEMPERATURE: return "SetTemperature";
            case HvacMethods.GET_FAN_SPEED: return "GetFanSpeed";
            case HvacMethods.SET_FAN_SPEED: return "SetFanSpeed";
            case HvacMethods.GET_AC_STATUS: return "GetACStatus";
            case HvacMethods.SET_AC_STATUS: return "SetACStatus";
            default: return "Unknown HVAC Method";
        }
    }
    
    private static String getDoorMethodName(short methodId) {
        switch (methodId) {
            case DoorMethods.GET_DOOR_STATUS: return "GetDoorStatus";
            case DoorMethods.LOCK_DOORS: return "LockDoors";
            case DoorMethods.UNLOCK_DOORS: return "UnlockDoors";
            case DoorMethods.GET_WINDOW_STATUS: return "GetWindowStatus";
            case DoorMethods.CONTROL_WINDOW: return "ControlWindow";
            default: return "Unknown Door Method";
        }
    }
    
    private static String getEngineMethodName(short methodId) {
        switch (methodId) {
            case EngineMethods.GET_ENGINE_STATUS: return "GetEngineStatus";
            case EngineMethods.GET_RPM: return "GetRPM";
            case EngineMethods.GET_SPEED: return "GetSpeed";
            case EngineMethods.GET_FUEL_LEVEL: return "GetFuelLevel";
            case EngineMethods.GET_ENGINE_TEMPERATURE: return "GetEngineTemperature";
            default: return "Unknown Engine Method";
        }
    }
    
    private static String getNavigationMethodName(short methodId) {
        switch (methodId) {
            case NavigationMethods.SET_DESTINATION: return "SetDestination";
            case NavigationMethods.GET_CURRENT_POSITION: return "GetCurrentPosition";
            case NavigationMethods.START_NAVIGATION: return "StartNavigation";
            case NavigationMethods.STOP_NAVIGATION: return "StopNavigation";
            case NavigationMethods.GET_ROUTE_INFO: return "GetRouteInfo";
            default: return "Unknown Navigation Method";
        }
    }
    
    private static String getMediaMethodName(short methodId) {
        switch (methodId) {
            case MediaMethods.PLAY: return "Play";
            case MediaMethods.PAUSE: return "Pause";
            case MediaMethods.STOP: return "Stop";
            case MediaMethods.NEXT_TRACK: return "NextTrack";
            case MediaMethods.PREVIOUS_TRACK: return "PreviousTrack";
            case MediaMethods.SET_VOLUME: return "SetVolume";
            case MediaMethods.GET_VOLUME: return "GetVolume";
            case MediaMethods.GET_CURRENT_TRACK: return "GetCurrentTrack";
            default: return "Unknown Media Method";
        }
    }
} 