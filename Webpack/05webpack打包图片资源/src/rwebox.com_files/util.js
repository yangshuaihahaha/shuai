<!DOCTYPE html>








<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <title>Real World Evidence | Login Form</title>

    <meta name="_csrf" content=""/>
    <meta name="_csrf_header" content=""/>


    
  
  <link rel="stylesheet" href="/cas/css/cas.css;jsessionid=271C7C36E51E15A297C3AF7C07B7E537" />
  <link rel="icon" href="/cas/favicon.ico;jsessionid=271C7C36E51E15A297C3AF7C07B7E537" type="image/x-icon" />
  <link rel="stylesheet" href="/cas/css/login.css" />
  <link rel="stylesheet" href="/cas/css/login-soft.css" />
  <link rel="stylesheet" href="/cas/css/components-rounded.css" />
  <link rel="stylesheet" href="/cas/css/bootstrap.min.css" />
  
</head>
<body id="cas">

<div id="container">
  <header>
    <a id="logo" href="#"/>">RWE</a>
  </header>
  <div id="content">


<!--
    <div id="msg" class="errors">
        <h2>Non-secure Connection</h2>
        <p>You are currently accessing CAS over a non-secure connection. Single Sign On WILL NOT WORK. In order to have single sign on work, you MUST log in over HTTPS.</p>
    </div>
 -->

<div id="cookiesDisabled" class="errors" style="display:none;">
    <h2>Browser cookies disabled</h2>
    <p>Your browser does not accept cookies. Single Sign On WILL NOT WORK.</p>
</div>




<div class="box" id="login">
    <form id="fm1" cssstyle="login-form" action="/cas/login;jsessionid=271C7C36E51E15A297C3AF7C07B7E537?service=http%3A%2F%2Flocalhost%3A8080%2Frwe-web%2Flogin%2Fcas" method="post">

        

        <section class="row">
            
                
                
                    
                <div class="input-icon input-icon-md">
                    <div class="div-input-text-sep-line"></div>
                    <i class="icon-rwe-login-username"></i>
                    <input id="username" name="username" class="required form-control form-control-solid  input-shadow" tabindex="1" placeholder="Username" accesskey="u" type="text" value="" autocomplete="off"/>
                </div>
				
            
        </section>

        <section class="row">
                
            
            <div class="input-icon input-icon-md">
                <div class="div-input-text-sep-line"></div>
                <i class="icon-rwe-login-password"></i>
                <input id="password" name="password" class="required form-control form-control-solid placeholder-no-fix input-shadow bg-white" tabindex="2" placeholder="Password" accesskey="p" type="password" value="" autocomplete="off"/>
            </div>
            <span id="capslock-on" style="display:none;"><p>CAPSLOCK key is turned on!</p></span>
        </section>
 
        <section class="row" style="padding-bottom: 0;margin-top: 6px;">
            <div class="form-actions">
                <div style="color: #6d7b88;">
                    <input type="checkbox" name="rememberMe" id="rememberMe" value="true" tabindex="5" style="right: 315px;"  class="chk_1" />
                    <label for="rememberMe"></label>Remember Me
					<a href="http://dev-rwe.rwebox.com/rwe-web/forgotPassword">Forgot password?</a>
                </div>
            </div>
        </section>
		
        <section class="row" style="padding-bottom: 0;margin-top: 6px;">
			<div class="form-actions">
				<div>
					<a href="http://dev-rwe.rwebox.com/rwe-web/terms/services.page" target="_blank">EULA</a> & <a href="http://dev-rwe.rwebox.com/rwe-web/privacy/policy.page" target="_blank">Privacy Policy</a>
				</div>
			</div>
		</section>
		
        <section class="row btn-row">
            <input type="hidden" name="lt" value="LT-435-IpcVEJpb1wvhQllTcFabZSxwMXVz3A-rwe.rwebox.com" />
            <input type="hidden" name="execution" value="e1s1" />
            <input type="hidden" name="_eventId" value="submit" />

            <input class="btn-submit2" name="submit" accesskey="l" value="Login" tabindex="6" type="submit" />
        </section>
    </form>
</div>





</div> <!-- END #content -->

<footer>
  <div id="copyright" class="footer" style="text-align:center;color:#6D7B88;margin-top:105px;margin-left: -50px;">
    
    <span class="new-color-black">沪ICP备13035915号-2</span>
  </div>
</footer>

</div> <!-- END #container -->

<script src="https://cdnjs.cloudflare.com/ajax/libs/headjs/1.0.3/head.min.js"></script>

<script type="text/javascript" src="/cas/js/cas.js;jsessionid=271C7C36E51E15A297C3AF7C07B7E537"></script>

</body>
</html>


