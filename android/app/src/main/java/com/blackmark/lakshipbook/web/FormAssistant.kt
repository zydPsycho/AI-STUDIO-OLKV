package com.blackmark.lakshipbook.web

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.blackmark.lakshipbook.data.Passenger
import org.json.JSONObject

class FormAssistant(
    private val onSecurityStepDetected: (SecurityStep) -> Unit,
    private val onAssistantMessage: (String) -> Unit
) {
    enum class SecurityStep { CAPTCHA, OTP }

    fun inspect(webView: WebView) {
        webView.evaluateJavascript(INSPECT_SCRIPT) { raw ->
            val result = raw.orEmpty().lowercase()
            when {
                result.contains("captcha") -> onSecurityStepDetected(SecurityStep.CAPTCHA)
                result.contains("otp") -> onSecurityStepDetected(SecurityStep.OTP)
                result.contains("no-compatible-fields") -> onAssistantMessage("No compatible passenger fields found. Please enter details manually.")
            }
        }
    }

    fun fillPassenger(webView: WebView, passenger: Passenger) {
        val profile = JSONObject().apply {
            put("name", passenger.name)
            put("dob", passenger.dateOfBirth)
            put("gender", passenger.gender)
            put("mobile", passenger.mobile)
            put("email", passenger.email)
            put("address", passenger.address)
            put("idtype", passenger.idType)
            put("idnumber", passenger.idNumber)
            put("nationality", passenger.nationality)
        }
        val safeJson = JSONObject.quote(profile.toString())
        webView.evaluateJavascript("fillLakShipSafe($safeJson)") { raw ->
            val result = raw.orEmpty().lowercase()
            if (result.contains("security-step")) {
                when {
                    result.contains("captcha") -> onSecurityStepDetected(SecurityStep.CAPTCHA)
                    result.contains("otp") -> onSecurityStepDetected(SecurityStep.OTP)
                }
            } else if (result.contains("no-compatible-fields")) {
                onAssistantMessage("The portal fields could not be matched safely. Manual entry is required.")
            } else {
                onAssistantMessage("Compatible fields filled. Review every value on the official website before continuing.")
            }
        }
    }

    companion object {
        private const val INSPECT_SCRIPT = """
            (function() {
              const text = document.body ? document.body.innerText.toLowerCase() : '';
              const hasCaptcha = text.includes('captcha') || !!document.querySelector('[id*="captcha" i], [name*="captcha" i]');
              const hasOtp = text.includes('one time password') || text.includes('otp') || !!document.querySelector('[id*="otp" i], [name*="otp" i]');
              const fields = document.querySelectorAll('input, select, textarea').length;
              if (hasCaptcha) return 'captcha';
              if (hasOtp) return 'otp';
              return fields ? 'ready' : 'no-compatible-fields';
            })()
        """

        private val SAFE_SCRIPT = """
            (function() {
              window.fillLakShipSafe = function(json) {
                const p = JSON.parse(json);
                const nodes = Array.from(document.querySelectorAll('input, select, textarea'));
                const securityPattern = /(captcha|otp|one.?time|password|passwd|card|cvv|upi|pin|payment|transaction)/i;
                const metadata = (el) => ((el.name || '') + ' ' + (el.id || '') + ' ' + (el.getAttribute('aria-label') || '') + ' ' + (el.placeholder || '') + ' ' + (el.labels && el.labels[0] ? el.labels[0].innerText : '')).toLowerCase();
                if (nodes.some(el => securityPattern.test(metadata(el)))) return 'security-step';
                const matchers = [
                  [['first name','given name','full name','passenger name','name'], p.name],
                  [['date of birth','dob','birth date'], p.dob],
                  [['gender','sex'], p.gender],
                  [['mobile','phone','telephone','contact'], p.mobile],
                  [['email','e-mail'], p.email],
                  [['address','street','locality'], p.address],
                  [['id type','identity type','document type'], p.idtype],
                  [['id number','identity number','document number'], p.idnumber],
                  [['nationality','citizenship'], p.nationality]
                ];
                let count = 0;
                for (const [keys, value] of matchers) {
                  if (!value) continue;
                  const el = nodes.find(node => !securityPattern.test(metadata(node)) && keys.some(key => metadata(node).includes(key)));
                  if (!el) continue;
                  if (el.tagName === 'SELECT') {
                    const option = Array.from(el.options).find(o => (o.text || '').toLowerCase().includes(String(value).toLowerCase()));
                    if (option) el.value = option.value;
                  } else {
                    el.value = value;
                  }
                  el.dispatchEvent(new Event('input', {bubbles:true}));
                  el.dispatchEvent(new Event('change', {bubbles:true}));
                  count++;
                }
                return count ? 'filled-' + count : 'no-compatible-fields';
              };
              return 'ready';
            })()
        """

        fun install(webView: WebView) {
            webView.settings.javaScriptEnabled = true
            webView.evaluateJavascript(SAFE_SCRIPT, null)
        }
    }
}

class FormAssistantBridge {
    @JavascriptInterface
    fun onVerificationRequired(type: String) = Unit
}
