import Header from 'components/layouts/Header';
import Footer from 'components/layouts/Footer';
import Main from 'pages/MainPage';
import Login from 'pages/Login';
import QnABoard from 'pages/qnABoard/QnABoard';
import FreeBoard from 'pages/freeBoard/FreeBoard';
import PostList from 'components/board/postList';
import PostContent from 'components/board/postContent';
import ReviewPage from 'pages/review/ReviewPage';
import ReviewPageDetail from 'pages/review/ReviewPageDetail';
import CreateTeacher from 'pages/review/CreateTeacher';
import StudentSignUpForm from 'components/member/signup/StudentSignUpForm';
import TeacherSignUpForm from 'components/member/signup/TeacherSignUpForm';
import UpdateTeacher from 'pages/review/UpdateTeacher';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import SignUp from 'pages/SignUp';
import SelectSignUpType from 'components/member/signup/SelectSignUpType';
import Information from 'pages/review/TeacherDetail/Information';
import Lectures from 'pages/review/TeacherDetail/Lectures';
import TeacherReview from 'pages/review/TeacherDetail/TeacherReview';

function Router() {
  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/login" element={<Login />} />

        <Route path="signup/*" element={<SignUp />}>
          <Route path="" element={<SelectSignUpType />} />
          <Route path="student" element={<StudentSignUpForm />} />
          <Route path="teacher" element={<TeacherSignUpForm />} />
        </Route>

        <Route path="/qna" element={<QnABoard />} />

        <Route path="free/*" element={<FreeBoard />}>
          <Route path="write" element={<PostList />} />
          <Route path="write" element={<PostContent />} />
          <Route path="edit" element={<PostContent />} />
        </Route>


        <Route path="/ReviewPage" element={<ReviewPage />} />
        <Route path="/ReviewPage/createTeacher" element={<CreateTeacher />} />
        <Route
          path="/ReviewPage/updateTeacher/:teacherId"
          element={<UpdateTeacher />}
        />
        <Route
          path="ReviewPageDetail/:teacherId/*"
          element={<ReviewPageDetail />}
        >
          <Route path="" element={<Information />} />
          <Route path="lectures" element={<Lectures />} />
          <Route path="teacherReview" element={<TeacherReview />} />
        </Route>
      </Routes>
      <Footer />
    </BrowserRouter>
  );
}

export default Router;
