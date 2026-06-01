import { useMemo, useState } from "react";

import { Link } from "react-router-dom";

import { forgotPassword } from "../api/authApi";

import { GENERIC_ERROR_RETRY, INVALID_FIELD_MESSAGE } from "../constants/authUiStrings";

import { validateEmail, validateForgotPasswordForm } from "../schemas/authSchemas";

import { APP_ROUTES } from "../../../shared/constants/routes";



const PRIVACY_SAFE_SUCCESS_MESSAGE =

  "Nếu email hợp lệ, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.";

const ERROR_MESSAGE_BY_CODE = {

  429: "Bạn thao tác quá nhanh, vui lòng thử lại sau.",

  500: GENERIC_ERROR_RETRY,

};



function resolveFieldErrors(errors = []) {

  return errors.reduce((acc, item) => {

    if (item?.field && !acc[item.field]) {

      acc[item.field] = item.reason || INVALID_FIELD_MESSAGE;

    }

    return acc;

  }, {});

}



export function ForgotPasswordPage() {

  const [form, setForm] = useState({ email: "" });

  const [errors, setErrors] = useState({ email: "" });

  const [globalError, setGlobalError] = useState("");

  const [successMessage, setSuccessMessage] = useState("");

  const [isSubmitting, setIsSubmitting] = useState(false);



  const validation = useMemo(() => validateForgotPasswordForm(form), [form]);

  const isSubmitDisabled = !validation.isValid || isSubmitting;



  const onChangeEmail = (event) => {

    const nextValue = event.target.value;

    setForm({ email: nextValue });

    setErrors({ email: "" });

    setGlobalError("");

    setSuccessMessage("");

  };



  const onBlurEmail = () => {

    setErrors({ email: validateEmail(form.email.trim()) });

  };



  const getErrorMessage = (error) => {

    if (error?.code === 400) {

      return error?.message || "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại email.";

    }

    return ERROR_MESSAGE_BY_CODE[error?.code] || GENERIC_ERROR_RETRY;

  };



  const onSubmit = async (event) => {

    event.preventDefault();

    if (isSubmitting) return;



    const normalizedForm = { email: form.email.trim().toLowerCase() };

    const nextValidation = validateForgotPasswordForm(normalizedForm);

    setErrors(nextValidation.errors);

    setGlobalError("");

    setSuccessMessage("");

    if (!nextValidation.isValid) return;



    setIsSubmitting(true);

    try {

      await forgotPassword(normalizedForm);

      setSuccessMessage(PRIVACY_SAFE_SUCCESS_MESSAGE);

    } catch (error) {

      const serverFieldErrors = resolveFieldErrors(error?.errors);

      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {

        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));

      }

      setGlobalError(getErrorMessage(error));

    } finally {

      setIsSubmitting(false);

    }

  };



  return (

    <div className="bg-background text-on-background">

      <main className="flex items-center justify-center px-4 py-10 sm:px-6">

        <section className="w-full max-w-[500px] rounded-lg border border-outline-variant bg-white p-6 shadow-sm sm:p-8">

          <header className="text-center">

            <h1 className="text-5xl font-semibold text-on-surface">Quên mật khẩu</h1>

            <p className="mt-3 text-base leading-7 text-on-surface-variant">

              Nhập địa chỉ email của bạn, chúng tôi sẽ gửi liên kết để đặt lại mật khẩu.

            </p>

          </header>



          {globalError ? (

            <div className="mt-6 rounded border border-error bg-error-container px-4 py-3 text-sm text-on-error-container">

              {globalError}

            </div>

          ) : null}



          {successMessage ? (

            <div className="mt-6 rounded border border-primary bg-surface-container px-4 py-3 text-sm text-on-surface">

              {successMessage}

            </div>

          ) : null}



          <form className="mt-6 space-y-4" onSubmit={onSubmit} noValidate>

            <div className="space-y-2">

              <label htmlFor="forgot-password-email" className="block text-sm font-medium text-on-surface">

                Email

              </label>

              <input

                id="forgot-password-email"

                name="email"

                type="email"

                value={form.email}

                onChange={onChangeEmail}

                onBlur={onBlurEmail}

                placeholder="user@example.com"

                disabled={isSubmitting}

                aria-invalid={Boolean(errors.email)}

                aria-describedby={errors.email ? "forgot-password-email-error" : undefined}

                className={[

                  "w-full rounded border bg-white px-3 py-3 text-base outline-none transition",

                  errors.email ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",

                  isSubmitting ? "cursor-not-allowed opacity-70" : "",

                ].join(" ")}

              />

              {errors.email ? (

                <p id="forgot-password-email-error" className="text-xs text-error">

                  {errors.email}

                </p>

              ) : null}

            </div>



            <button

              type="submit"

              disabled={isSubmitDisabled}

              className="mt-1 flex w-full items-center justify-center gap-2 rounded bg-primary-container px-4 py-3 text-sm font-semibold text-on-primary-container transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-75"

            >

              {isSubmitting ? (

                <>

                  <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white/50 border-t-white" />

                  <span>Đang gửi liên kết...</span>

                </>

              ) : (

                "Gửi liên kết đặt lại"

              )}

            </button>

          </form>



          <div className="mt-8 text-center">

            <Link

              to={APP_ROUTES.login}

              className="inline-flex items-center justify-center gap-1 text-sm font-medium text-primary hover:underline"

            >

              <span aria-hidden="true">←</span>

              <span>Quay lại đăng nhập</span>

            </Link>

          </div>

        </section>

      </main>

    </div>

  );

}

