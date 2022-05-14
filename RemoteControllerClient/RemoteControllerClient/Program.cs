using System.Drawing;
using System.Net;
using System.Net.Sockets;
using System.Text;
using Newtonsoft.Json.Linq;
using SharpDX.XInput;

class XInputController
{
    private static int JOYSTICK_DIVIDER = 257;
    
    private Controller controller;
    private Gamepad gamepad;
    private bool connected = false;
    private int deadband = 2500;
    private Point leftThumb, rightThumb = new Point(0,0);
    private float leftTrigger, rightTrigger;
    private bool a, b, x, y, rb, lb, lt, rt, start, select, ddown, dup, dleft, dright;
    private UdpClient UdpClient;
    private bool changed;
    private IPEndPoint ipEndPoint;
    
    public XInputController()
    {
        JObject o1 = JObject.Parse(File.ReadAllText(@"config.json"));
        String host = (string)o1.GetValue("server");
        if ((bool)o1.GetValue("isDomain"))
        {
            host = Dns.GetHostAddresses(host)[0].ToString();
        }
        ipEndPoint = new IPEndPoint(IPAddress.Parse(host), (int)o1.GetValue("port"));
        UdpClient = new UdpClient();
        controller = new Controller(UserIndex.One);
        connected = controller.IsConnected;
        new Thread(() =>
        {
            while (true)
            {
                Thread.Sleep(1);
                Update();
            }
        }).Start();
    }

    // Call this method to update all class values
    public void Update()
    {
        if(!connected) 
            return;
        gamepad = controller.GetState().Gamepad;
        changed = false;

        var leftthX= get(gamepad.LeftThumbX);
        var leftthY= get(gamepad.LeftThumbY);
        if (leftThumb.X / JOYSTICK_DIVIDER != leftthX / JOYSTICK_DIVIDER || leftThumb.Y / JOYSTICK_DIVIDER != leftthY / JOYSTICK_DIVIDER)
        {
            leftThumb.X  = leftthX;
            leftThumb.Y  = leftthY;
            send("la", $"{leftThumb.X};{leftThumb.Y}");
        }
        var rightthX= get(gamepad.RightThumbX);
        var rightthY= get(gamepad.RightThumbY);
        if (rightThumb.X / JOYSTICK_DIVIDER != rightthX / JOYSTICK_DIVIDER || rightThumb.Y / JOYSTICK_DIVIDER != rightthY / JOYSTICK_DIVIDER)
        {
            rightThumb.X  = rightthX;
            rightThumb.Y  = rightthY;
            send("ra", $"{rightThumb.X};{rightThumb.Y}");
        }
        var rightTrigger= getTrigger(gamepad.RightTrigger);
        if (this.rightTrigger != rightTrigger)
        {
            this.rightTrigger  = rightTrigger;
            send("tr", this.rightTrigger);
        }
        var leftTrigger= getTrigger(gamepad.LeftTrigger);
        if (this.leftTrigger != leftTrigger)
        {
            this.leftTrigger  = leftTrigger;
            send("tl", this.leftTrigger);
        }
        var butA= (gamepad.Buttons & GamepadButtonFlags.A) != 0;
        if (a != butA)
        {
            a  = butA;
            send("ba", a.GetHashCode());
        }
        var butB= (gamepad.Buttons & GamepadButtonFlags.B) != 0;
        if (b != butB)
        {
            b  = butB;
            send("bb", b.GetHashCode());
        }
        var butX= (gamepad.Buttons & GamepadButtonFlags.X) != 0;
        if (x != butX)
        {
            x  = butX;
            send("bx", x.GetHashCode());
        }
        var butY= (gamepad.Buttons & GamepadButtonFlags.Y) != 0;
        if (y != butY)
        {
            y  = butY;
            send("by", y.GetHashCode());
        }
        var butrb= (gamepad.Buttons & GamepadButtonFlags.RightShoulder) != 0;
        if (rb != butrb)
        {
            rb  = butrb;
            send("rb", rb.GetHashCode());
        }
        var butlb= (gamepad.Buttons & GamepadButtonFlags.LeftShoulder) != 0;
        if (lb != butlb)
        {
            lb  = butlb;
            send("lb", lb.GetHashCode());
        }
        var butStart= (gamepad.Buttons & GamepadButtonFlags.Start) != 0;
        if (start != butStart)
        {
            start  = butStart;
            send("bs", start.GetHashCode());
        }
        var butSel= (gamepad.Buttons & GamepadButtonFlags.Back) != 0;
        if (select != butSel)
        {
            select  = butSel;
            send("bp", select.GetHashCode());
        }
        var butDDown= (gamepad.Buttons & GamepadButtonFlags.DPadDown) != 0;
        var butDUp= (gamepad.Buttons & GamepadButtonFlags.DPadUp) != 0;
        var butDRight= (gamepad.Buttons & GamepadButtonFlags.DPadRight) != 0;
        var butDLeft= (gamepad.Buttons & GamepadButtonFlags.DPadLeft) != 0;
        if (ddown != butDDown || dup != butDUp || dright != butDRight || dleft != butDLeft)
        {
            ddown  = butDDown;
            dup  = butDUp;
            dright  = butDRight;
            dleft  = butDLeft;
            send("dp", dleft.GetHashCode() + ";"+dup.GetHashCode() + ";"+dright.GetHashCode() + ";"+ddown.GetHashCode());
        }
        var butlt= (gamepad.Buttons & GamepadButtonFlags.LeftThumb) != 0;
        if (lt != butlt)
        {
            lt  = butlt;
            send("lt", lt.GetHashCode());
        }
        var butrt= (gamepad.Buttons & GamepadButtonFlags.RightThumb) != 0;
        if (rt != butrt)
        {
            rt  = butrt;
            send("rt", rt.GetHashCode());
        }

        if (changed)
        {
            send("se", "");
        }
        
    }

    private int get(short num)
    {
        return Math.Abs(num-1) < deadband
            ? 0
            : (int)((float)num);
    }
    
    private int getTrigger(short num)
    {
        return Math.Abs(num-1) < 25
            ? 0
            : num;
    }

    private void send(string prefix, object value)
    {
        changed = true;
        var datagram = Encoding.ASCII.GetBytes($"{prefix}{value}");
        UdpClient.Send(datagram, datagram.Length, ipEndPoint);
        Console.WriteLine($"{prefix}{value}");
    }
}

class Program
{
    public static void Main(string[] args)
    {
        XInputController controller = new XInputController();
    }
}